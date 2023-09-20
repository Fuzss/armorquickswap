package fuzs.armorquickswap.handler;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.item.v2.ToolTypeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class RightClickHarvestHandler {
    private static final Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
    private static final List<BlockPos> POSITION_OFFSETS = BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1)
            .map(BlockPos::immutable)
            .sorted(Comparator.comparingInt(t -> t.distManhattan(BlockPos.ZERO)))
            .toList();

    public static EventResultHolder<InteractionResult> onUseBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult) {

        if (player.isShiftKeyDown() || !ToolTypeHelper.INSTANCE.isHoe(player.getItemInHand(interactionHand))) return EventResultHolder.pass();

        BlockPos pos = hitResult.getBlockPos();
        Block block = level.getBlockState(pos).getBlock();

        boolean result = false;
        for (BlockPos offset : POSITION_OFFSETS) {

            result |= tryApplyToPosition(player, level, interactionHand, hitResult, pos.offset(offset), block);
            if (!result && offset.equals(BlockPos.ZERO)) {

                break;
            }
        }

        return result ? EventResultHolder.interrupt(InteractionResult.sidedSuccess(level.isClientSide)) : EventResultHolder.pass();
    }

    private static boolean tryApplyToPosition(Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult, BlockPos pos, Block block) {

        BlockState state = level.getBlockState(pos);
        if (state.is(block) && state.is(ModRegistry.REPLANTABLES_BLOCK_TAG)) {

            IntegerProperty age = findProperty(state, IntegerProperty.class, "age");
            int max = age != null ? age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0) : 0;
            if (age == null || max >= 15 || state.getValue(age) == max) {

                if (!level.isClientSide) {

                    if (!((ServerPlayer) player).gameMode.destroyBlock(pos)) return false;

                    tickNeighbourShapes((ServerLevel) level, pos, state.getBlock());

                    DirectionProperty facing = findProperty(state, DirectionProperty.class, "facing");
                    Direction[] directions = facing != null ? new Direction[]{state.getValue(facing)} : Direction.values();

                    List<ItemStack> items = List.of(new ItemStack(state.getBlock().asItem()), state.getBlock().getCloneItemStack(level, pos, state));

                    if (player.isCreative()) {

                        for (ItemStack itemStack : items) {

                            if (tryPlaceBlockItem(player, interactionHand, hitResult, pos, directions, itemStack)) {

                                break;
                            }
                        }
                    } else {

                        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos), ItemEntity::hasPickUpDelay);

                        for (ItemEntity entity : entities) {

                            ItemStack itemStack = entity.getItem();
                            if (itemStack.getItem() instanceof BlockItem && items.stream().anyMatch(t -> ItemStack.isSameItem(t, itemStack))) {

                                if (tryPlaceBlockItem(player, interactionHand, hitResult, pos, directions, itemStack)) {

                                    if (itemStack.isEmpty()) {

                                        entity.discard();
                                    }

                                    break;
                                }
                            }
                        }
                    }


                    ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(pos, level.getBlockState(pos)));
                    if (state != level.getBlockState(pos)) {

                        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    } else {

                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private static boolean tryPlaceBlockItem(Player player, InteractionHand interactionHand, BlockHitResult hitResult, BlockPos pos, Direction[] directions, ItemStack itemStack) {
        for (Direction direction : directions) {
            BlockHitResult blockHitResult = hitResult.withPosition(pos.relative(direction)).withDirection(direction.getOpposite());
            InteractionResult result = ((BlockItem) itemStack.getItem()).place(new BlockPlaceContext(player, interactionHand, itemStack, blockHitResult));
            if (result.consumesAction()) {
                return true;
            }
        }
        return false;
    }

    public static void tickNeighbourShapes(ServerLevel level, BlockPos pos, Block block) {

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : UPDATE_ORDER) {
            mutableBlockPos.setWithOffset(pos, direction);
            BlockState state = level.getBlockState(mutableBlockPos);
            if (state.is(block)) {
                state.tick(level, mutableBlockPos, level.getRandom());
            }
        }
    }

    @Nullable
    private static <T extends Property<?>> T findProperty(BlockState state, Class<T> clazz, String preferredName) {

        T property = null;
        for (Property<?> currentProperty : state.getProperties()) {

            if (clazz.isInstance(currentProperty)) {

                if (currentProperty.getName().equals(preferredName)) {

                    return clazz.cast(currentProperty);
                } else if (property == null) {

                    property = clazz.cast(currentProperty);
                }
            }
        }

        return property;
    }
}
