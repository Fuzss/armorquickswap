package fuzs.armorquickswap.client.handler;

import fuzs.armorquickswap.client.ClientEntityData;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class ShatterTickHandler {
    public static final int SHATTER_DEATH_TIME = 100;
    public static final Vec3 DELTA_MOVEMENT_SCALE = new Vec3(0.2, 0.05, 0.2);
    private static final int NOT_ON_GROUND_DELAY = 10;

    public static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {

        if (!entity.level().isClientSide || !ShatterRenderHandler.supportsEntityType(entity.getType())) {
            return EventResult.PASS;
        }

        // add a delay to entities that are falling, during this delay vanilla is still allowed to tick the entity normally
        // to hopefully have it reach the actual death position client-side to allow the death animation to play there.
        // this mainly solves an issue with mobs dying from fall damage, where the mob is already dead, but is still falling on the client,
        // resulting in the mob stopping and playing the death animation in the air above the actual death position.
        entity.deathTime = entity.onGround() ? NOT_ON_GROUND_DELAY : 0;

        // enable no physics so the death animation is not hindered by terrain
        entity.noPhysics = true;

        ClientEntityData.submitEntity(entity);

        return EventResult.PASS;
    }

    public static EventResult onLivingTick(LivingEntity entity) {

        if (!entity.level().isClientSide || !entity.isDeadOrDying() || !ShatterRenderHandler.supportsEntityType(entity.getType())) {
            return EventResult.PASS;
        }

        if (entity.deathTime >= SHATTER_DEATH_TIME) {

            // we are done with the entity, now remove it properly from the world
            ClientEntityData.clearEntity(entity);

            return EventResult.INTERRUPT;
        } else {

            Vec3 deltaMovement = ClientEntityData.getAndUpdateDeltaMovement(entity).multiply(DELTA_MOVEMENT_SCALE);

            // just set this, should relate to rendering, but does not seem to have any noticeable effect, but just keep it for now
            entity.xo = entity.getX();
            entity.yo = entity.getY();
            entity.zo = entity.getZ();

            // while the delay is not reached allow vanilla to continue ticking the entity,
            // so it can properly reach the actual death position client-side (or at least get closer)
            if (entity.deathTime >= NOT_ON_GROUND_DELAY) {

                // we need to advance the death time ourselves now as vanilla tick is no longer running
                entity.deathTime++;
                entity.move(MoverType.SELF, deltaMovement);

                return EventResult.INTERRUPT;
            }

            return EventResult.PASS;
        }
    }
}
