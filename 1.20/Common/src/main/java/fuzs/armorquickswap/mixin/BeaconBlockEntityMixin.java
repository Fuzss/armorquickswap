package fuzs.armorquickswap.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconBlockEntityMixin extends BlockEntity {

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void applyEffects(Level level, BlockPos pos, int levels, @Nullable MobEffect primary, @Nullable MobEffect secondary, CallbackInfo callback, double beaconRange, int amplifier, int duration, AABB aABB) {

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aABB, entity -> entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerUUID() != null);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(primary, duration, amplifier, true, true));
        }

        if (levels >= 4 && primary != secondary && secondary != null) {

            for (LivingEntity entity : entities) {
                entity.addEffect(new MobEffectInstance(secondary, duration, 0, true, true));
            }
        }
    }
}
