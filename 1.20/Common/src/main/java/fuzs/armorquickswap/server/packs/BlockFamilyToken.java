package fuzs.armorquickswap.server.packs;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record BlockFamilyToken(Class<? extends Block> clazz, String prefix, String postfix, String regex) {
    public static final BlockFamilyToken PLANKS = of("_planks");
    public static final BlockFamilyToken LOG = of(RotatedPillarBlock.class, "_log");
    public static final BlockFamilyToken WOOD = of(RotatedPillarBlock.class, "_wood");
    public static final BlockFamilyToken STRIPPED_LOG = of(RotatedPillarBlock.class, "stripped_", "_log");
    public static final BlockFamilyToken STRIPPED_WOOD = of(RotatedPillarBlock.class, "stripped_", "_wood");
    public static final BlockFamilyToken SIGN = of(StandingSignBlock.class, "_sign");
    public static final BlockFamilyToken HANGING_SIGN = of(CeilingHangingSignBlock.class, "_hanging_sign");
    public static final BlockFamilyToken PRESSURE_PLATE = of(PressurePlateBlock.class, "_pressure_plate");
    public static final BlockFamilyToken TRAPDOOR = of(TrapDoorBlock.class, "_trapdoor");
    public static final BlockFamilyToken STAIRS = of(StairBlock.class, "_stairs");
    public static final BlockFamilyToken BUTTON = of(ButtonBlock.class, "_button");
    public static final BlockFamilyToken SLAB = of(SlabBlock.class, "_slab");
    public static final BlockFamilyToken FENCE_GATE = of(FenceGateBlock.class, "_fence_gate");
    public static final BlockFamilyToken FENCE = of(FenceBlock.class, "_fence");
    public static final BlockFamilyToken DOOR = of(DoorBlock.class, "_door");
    public static final Collection<BlockFamilyToken> WOOD_FAMILY = Stream.of(PLANKS, LOG, WOOD, STRIPPED_LOG, STRIPPED_WOOD, SIGN, HANGING_SIGN, PRESSURE_PLATE, TRAPDOOR, STAIRS, BUTTON, SLAB, FENCE_GATE, FENCE, DOOR).sorted(Comparator.comparing(t -> t.prefix.isEmpty())).toList();

    public static BlockFamilyToken of(String postfix) {
        return of(Block.class, postfix);
    }

    public static BlockFamilyToken of(Class<? extends Block> clazz, String postfix) {
        return of(clazz, "", postfix);
    }

    public static BlockFamilyToken of(String prefix, String postfix) {
        return of(Block.class, prefix, postfix);
    }

    public static BlockFamilyToken of(Class<? extends Block> clazz, String prefix, String postfix) {
        return new BlockFamilyToken(clazz, prefix, postfix, "^" + prefix + "[a-z0-9/._-]+" + postfix + "$");
    }

    public boolean test(String path, Block block, Collection<Function<Block, IndicatorType>> indicators) {
        return this.clazz.isInstance(block) && path.matches(this.regex) && IndicatorType.test(indicators, block);
    }

    public String strip(String s) {
        return s.replaceAll("^" + this.prefix, "").replaceAll(this.postfix + "$", "");
    }

    public enum IndicatorType {
        NONE(0), WEAK(1), MEDIUM(3), STRONG(6);

        public static final Collection<Function<Block, IndicatorType>> WOOD_FAMILY_INDICATORS;

        static {
            ImmutableList.Builder<Function<Block, IndicatorType>> builder = ImmutableList.builder();
            builder.add(strong(block -> block.defaultBlockState().instrument() == NoteBlockInstrument.BASEDRUM));
            builder.add(strong(block -> !block.defaultBlockState().requiresCorrectToolForDrops()));
            builder.add(medium(block -> block.defaultBlockState().getSoundType() == SoundType.WOOD));
            builder.add(medium(block -> block.defaultBlockState().ignitedByLava()));
            builder.add(weak(block -> block.defaultBlockState().getPistonPushReaction() == PushReaction.NORMAL));
            builder.add(strong(block -> !block.defaultBlockState().canBeReplaced()));
            WOOD_FAMILY_INDICATORS = builder.build();
        }

        static Function<Block, IndicatorType> weak(Predicate<Block> filter) {
            return block -> filter.test(block) ? WEAK : NONE;
        }

        static Function<Block, IndicatorType> medium(Predicate<Block> filter) {
            return block -> filter.test(block) ? MEDIUM : NONE;
        }

        static Function<Block, IndicatorType> strong(Predicate<Block> filter) {
            return block -> filter.test(block) ? STRONG : NONE;
        }

        private final int value;

        IndicatorType(int value) {
            this.value = value;
        }

        public static boolean test(Collection<Function<Block, IndicatorType>> indicators, Block block) {
            return value(indicators, block) >= indicators.size() * 2;
        }

        public static int value(Collection<Function<Block, IndicatorType>> indicators, Block block) {
            return indicators.stream()
                    .mapToInt(t -> t.apply(block).value)
                    .sum();
        }
    }
}
