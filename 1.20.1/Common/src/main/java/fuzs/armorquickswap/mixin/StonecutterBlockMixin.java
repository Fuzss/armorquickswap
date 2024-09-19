package fuzs.armorquickswap.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StonecutterBlock.class)
abstract class StonecutterBlockMixin extends Block {
    @Shadow
    @Final
    private static DirectionProperty FACING;
    @Unique
    private static final VoxelShape BLADE_SHAPE_X = Block.box(8.0 - 1.0E-6, 9.0, 1.0, 8.0 + 1.0E-6, 16.0, 15.0);
    @Unique
    private static final VoxelShape BLADE_SHAPE_Z = Block.box(1.0, 9.0, 8.0 - 1.0E-6, 15.0, 16.0, 8.0 + 1.0E-6);

    public StonecutterBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        VoxelShape shape = state.getValue(FACING).getAxis() == Direction.Axis.X ? BLADE_SHAPE_X : BLADE_SHAPE_Z;
        if (shape.toAabbs().stream().map(t -> t.move(pos)).anyMatch(t -> t.intersects(entity.getBoundingBox()))) {
            entity.hurt(level.damageSources().generic(), 1.0F);
        }
    }
}
