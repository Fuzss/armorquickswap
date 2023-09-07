package fuzs.armorquickswap.client.handler;

import fuzs.armorquickswap.client.ClientParticleTypeManager;
import fuzs.armorquickswap.client.init.ClientModRegistry;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

public class MobDismembermentHandler {

    public static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {

        if (!entity.level().isClientSide || ShatterRenderHandler.containedInBlacklist(entity.getType())) {
            return EventResult.PASS;
        }

        dismember((ClientLevel) entity.level(), entity, source.is(DamageTypeTags.IS_EXPLOSION));
        entity.setRemoved(Entity.RemovalReason.KILLED);

        return EventResult.PASS;
    }

    private static void dismember(ClientLevel level, LivingEntity entity, boolean deathByExplosion) {

        int particleAmount = (int) (Math.sqrt(entity.getBbWidth() * entity.getBbHeight()) * 50.0);
        if (deathByExplosion) particleAmount *= 10;
        for (int k = 0; k < particleAmount + level.random.nextInt(particleAmount); k++) {

            float multiplier = 0.3F;
            double motionX = -Mth.sin(entity.yBodyRotO / 180.0F * (float) Math.PI) * Mth.cos(entity.xRotO / 180.0F * (float) Math.PI) * multiplier;
            double motionZ = Mth.cos(entity.yBodyRotO / 180.0F * (float) Math.PI) * Mth.cos(entity.xRotO / 180.0F * (float) Math.PI) * multiplier;
            double motionY = -Mth.sin(entity.xRotO / 180.0F * (float) Math.PI) * multiplier + 0.1F;

            multiplier = 0.02F;
            float var5 = entity.getRandom().nextFloat() * (float) Math.PI * 2.0F;
            multiplier *= entity.getRandom().nextFloat();

            motionX += Math.cos(var5) * (double) multiplier;
            motionY += (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.1F;
            motionZ += Math.sin(var5) * (double) multiplier;

            Vec3 deltaMovement = entity.getDeltaMovement();
            if (entity.getMobType() != MobType.UNDEAD || entity instanceof Zombie) {
                Particle particle = ClientParticleTypeManager.INSTANCE.createParticle(ClientModRegistry.BLOOD_PARTICLE_TYPE, entity.getX(), entity.getY() + 0.5D + (entity.getRandom().nextDouble() * 0.7D), entity.getZ(), deltaMovement.x + motionX, deltaMovement.y + motionY, deltaMovement.z + motionZ);
                if (particle != null && entity instanceof Zombie) {
                    particle.setColor(1.0F, 1.0F, 0.0F);
                }
            }
        }
    }
}
