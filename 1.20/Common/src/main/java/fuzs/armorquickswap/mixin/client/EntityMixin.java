package fuzs.armorquickswap.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
abstract class EntityMixin {

    @ModifyVariable(method = "checkInsideBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onInsideBlock(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    protected BlockState checkInsideBlocks(BlockState blockState) {

        return blockState;
    }
}
