package fuzs.armorquickswap.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.handler.InventoryArmorClickHandler;
import fuzs.armorquickswap.client.handler.LocalArmorStandGearHandler;
import fuzs.armorquickswap.mixin.client.accessor.AgeableListModelAccessor;
import fuzs.armorquickswap.mixin.client.accessor.EntityAccessor;
import fuzs.armorquickswap.mixin.client.accessor.LivingEntityRendererAccessor;
import fuzs.armorquickswap.mixin.client.accessor.ModelPartAccessor;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.InteractionInputEvents;
import fuzs.puzzleslib.api.client.event.v1.RenderLivingEvents;
import fuzs.puzzleslib.api.client.event.v1.ScreenMouseEvents;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDeathCallback;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArmorQuickSwapClient implements ClientModConstructor {
    private static final Map<LivingEntity, ClientEntityData> LEVEL_CALLBACKS = new WeakHashMap<>();
    private static final int SHATTER_DEATH_TIME = 100;

    private static void registerHandlers() {
        // run before other mods like Quark that might interfere here
        // event phase must match PlayerInteractEvents#USE_ENTITY_AT as both use same underlying event on Fabric
        InteractionInputEvents.USE.register(EventPhase.BEFORE, LocalArmorStandGearHandler::onUseInteraction);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(InventoryArmorClickHandler::onBeforeMouseClick);


        LivingDeathCallback.EVENT.register((entity, source) -> {
            if (!entity.level().isClientSide) return EventResult.PASS;
            entity.deathTime = 0;
            entity.hurtTime = 0;
            EntityInLevelCallback callback = ((EntityAccessor) entity).armorquickswap$getLevelCallback();
            RemovalReasonHoldingNullCallback newCallback = new RemovalReasonHoldingNullCallback(entity, callback);
            entity.setLevelCallback(newCallback);
            EntityInLevelCallback forwardingCallback = new ForwardingEntityInLevelCallback(callback, newCallback);
            LEVEL_CALLBACKS.put(entity, new ClientEntityData(forwardingCallback, entity.getDeltaMovement(), entity.position()));
            entity.setSilent(true);

//            entity.setNoGravity(true);
//            entity.noPhysics = true;
//            entity.setSharedFlagOnFire(false);
//            entity.setInvisible(true);

            if (entity instanceof Mob mob) mob.setNoAi(true);
            return EventResult.PASS;
        });

        LivingEvents.TICK.register(entity -> {
            if (!entity.level().isClientSide) return EventResult.PASS;
            if (!entity.isDeadOrDying()) return EventResult.PASS;
            if (entity.deathTime >= SHATTER_DEATH_TIME) {
                ClientEntityData data = LEVEL_CALLBACKS.remove(entity);
                if (data != null) data.callback().onRemove(entity.getRemovalReason());
            } else {

//                entity.noPhysics = true;
//                entity.setNoGravity(true);
//                if (entity instanceof Mob mob) mob.setNoAi(true);

                entity.deathTime++;

//                ClientEntityData clientEntityData = LEVEL_CALLBACKS.get(entity);
//                if (clientEntityData != null) clientEntityData.moveEntity(entity);

                if (true) {

                    ClientEntityData clientEntityData = LEVEL_CALLBACKS.get(entity);
                    Vec3 deltaMovement = entity.getDeltaMovement();

                    if (deltaMovement.equals(clientEntityData.deltaMovement)) return EventResult.INTERRUPT;

                    if (Math.abs(deltaMovement.x) > Math.abs(clientEntityData.deltaMovement.x)) {
                        clientEntityData.deltaMovement = deltaMovement;
                    } else if (Math.abs(deltaMovement.z) > Math.abs(clientEntityData.deltaMovement.z)) {
                        clientEntityData.deltaMovement = deltaMovement;
                    } else if (deltaMovement.y > clientEntityData.deltaMovement.y) {
                        clientEntityData.deltaMovement = deltaMovement;
                    }

                    deltaMovement = clientEntityData.deltaMovement;

//                    if (clientEntityData.deltaMovement == null) {
//                        clientEntityData.deltaMovement = deltaMovement.multiply(0.4, 0.15, 0.4);
//                    }

//                    deltaMovement = clientEntityData.deltaMovement;

                    entity.xo = entity.getX();
                    entity.yo = entity.getY();
                    entity.zo = entity.getZ();

                    double pX = entity.getX() + deltaMovement.x * 0.2;
                    double pY = entity.getY() + deltaMovement.y * 0.05;
                    double pZ = entity.getZ() + deltaMovement.z * 0.2;

                    entity.setPos(pX, pY, pZ);

                    clientEntityData.deltaMovement = deltaMovement.scale(0.97);
//                    entity.setDeltaMovement(entity.getDeltaMovement().scale(0.97));
                }



//                double deathTimeScale = 1.0 - entity.deathTime / (float) SHATTER_DEATH_TIME;
//
//                deathTimeScale = deathTimeScale >= 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * deathTimeScale);
//
//                Vec3 scale = deltaMovement.multiply(0.4, 1.0, 0.4).scale(deathTimeScale);
//                if (scale.y < 0.0) scale = new Vec3(scale.x, 0.0, scale.z);
//                entity.move(MoverType.SELF, scale);
//
//                entity.setDeltaMovement(deltaMovement);
//
//                entity.xo = entity.getX();
//                entity.yo = entity.getY();
//                entity.zo = entity.getZ();
//
//                Vec3 motion = entity.getDeltaMovement();
//                double pX = entity.getX() + scale.x;
//                double pY = entity.getY() + scale.y;
//                double pZ = entity.getZ() + scale.z;
//
//                entity.setDeltaMovement(entity.getDeltaMovement().scale(0.97));
//                entity.setPos(pX, pY, pZ);
            }
            return EventResult.INTERRUPT;
        });


//        if (true) return;

        RenderLivingEvents.BEFORE.register(ArmorQuickSwapClient::onBeforeRenderEntity);
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> EventResult onBeforeRenderEntity(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {

        if (!entity.isDeadOrDying()) return EventResult.PASS;

        List<ModelPart> modelRootParts;
        if (renderer.getModel() instanceof HierarchicalModel<?> hierarchicalModel) {
            modelRootParts = List.of(hierarchicalModel.root());
        } else if (renderer.getModel() instanceof AgeableListModel<?> ageableListModel) {
            Iterable<ModelPart> bodyParts = ((AgeableListModelAccessor) ageableListModel).armorquickswap$callBodyParts();
            Iterable<ModelPart> headParts = ((AgeableListModelAccessor) ageableListModel).armorquickswap$callHeadParts();
            modelRootParts = ImmutableList.copyOf(Iterables.concat(bodyParts, headParts));
        } else {
            return EventResult.PASS;
        }

        // vanilla is very aggressive with syncing those shared flags, so we set them during rendering since they are used right after the entity is rendered
        // fire flag prevents rendering the fire overlay, mainly useful for undead mobs burning in the sun
        entity.setSharedFlagOnFire(false);
        // invisibility flag prevents the mob shadow from rendering which is not desired for the death animation
        // unfortunately the entity hitbox (F3+B) also no longer renders, but that's how it is
        entity.setInvisible(true);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        ((LivingEntityRendererAccessor<T, M>) renderer).armorquickswap$callScale(entity, poseStack, partialTick);
        poseStack.translate(0F, -1.501F, 0F);

        Map<ModelPart, PartPose> storedPoses = modelRootParts.stream().flatMap(ModelPart::getAllParts).collect(Collectors.toMap(Function.identity(), ModelPart::storePose));

        storedPoses.keySet().forEach(ModelPart::resetPose);

        List<ModelPart> modelParts = ModelPartHelper.explode(modelRootParts);

        modelParts.forEach(t -> t.yRot -= 12.0);

        float alpha = Mth.clamp((entity.deathTime + partialTick) / SHATTER_DEATH_TIME, 0.0F, 1.0F);
        RenderType renderType = RenderType.entityTranslucentCull(renderer.getTextureLocation(entity));
        render(modelParts, entity, entity.yBodyRot, poseStack, multiBufferSource.getBuffer(renderType), packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, alpha);

        storedPoses.forEach(ModelPart::loadPose);

        poseStack.popPose();

        return EventResult.INTERRUPT;
    }

    public static void render(List<ModelPart> modelList, LivingEntity entity, float rotationYaw, PoseStack poseStack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float progress) {

        poseStack.pushPose();


        poseStack.mulPose(Axis.YP.rotationDegrees(rotationYaw));

        Vec3 motion = entity.getDeltaMovement();
        float alpha = 1.0F - progress;
        for (int i = 0; i < modelList.size(); i++) {
            ModelPart modelPart = modelList.get(i);

            poseStack.pushPose();
            RandomSource rand = entity.getRandom();
            rand.setSeed((long) rand.nextInt() * entity.getId() * i * 1000);
            poseStack.translate(rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress * motion.z * 5D, rand.nextDouble() * progress * (motion.y + (rand.nextDouble() - 1.0D)), rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress * motion.x * 5D);
            float rotBase = 180F * rand.nextFloat() * progress;
            float rotX = rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress;
            float rotY = rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress;
            float rotZ = rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress;
            poseStack.mulPose(Axis.XP.rotationDegrees(rotBase * rotX));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotBase * rotY));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotBase * rotZ));

            poseStack.pushPose();
            modelPart.translateAndRotate(poseStack);
            ModelPartAccessor.class.cast(modelPart).armorquickswap$callCompile(poseStack.last(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            poseStack.popPose();

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private interface RemovalReasonHolder {

        Entity.RemovalReason getRemovalReason();
    }

    private static final class ClientEntityData {
        private final EntityInLevelCallback callback;
        public Vec3 deltaMovement;
        private double deltaX;
        private double deltaY;
        private double deltaZ;
        private Vec3 position;

        private ClientEntityData(EntityInLevelCallback callback, Vec3 deltaMovement, Vec3 position) {
            this.callback = callback;
            this.deltaMovement = deltaMovement;
            this.deltaX = deltaMovement.x;
            this.deltaY = deltaMovement.y;
            this.deltaZ = deltaMovement.z;
            this.position = position;
        }

        public double getDeathTimeScale(LivingEntity entity) {
            double deathTimeScale = 1.0 - entity.deathTime / (float) SHATTER_DEATH_TIME;
            return deathTimeScale >= 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * deathTimeScale);
        }

        public void moveEntity(LivingEntity entity) {
            entity.noPhysics = true;
            double deathTimeScale = this.getDeathTimeScale(entity);
//            ArmorQuickSwap.LOGGER.info("BEFORE: delta time scale {}, delta movement {}, position {}", deathTimeScale, deltaMovement, position);
            Vec3 deltaMovement = entity.getDeltaMovement();
            deltaMovement = new Vec3(Math.max(deltaX, deltaMovement.x), Math.max(0.0, Math.max(deltaY, deltaMovement.y)), Math.max(deltaZ, deltaMovement.z));
            Vec3 scale = deltaMovement.multiply(0.4, 0.15, 0.4).scale(deathTimeScale);
//            if (scale.y < 0.0)
//            scale = new Vec3(scale.x, 0, scale.z);
            entity.setPosRaw(this.position.x, this.position.y, this.position.z);
            entity.move(MoverType.SELF, scale);
            this.position = entity.position();
//            deltaMovement = deltaMovement.scale(0.98);
            this.deltaX = deltaMovement.x;
            this.deltaY = deltaMovement.y;
            this.deltaZ = deltaMovement.z;
//            ArmorQuickSwap.LOGGER.info("AFTER: delta time scale {}, delta movement {}, position {}", deathTimeScale, deltaMovement, position);
        }

        public EntityInLevelCallback callback() {
            return callback;
        }
    }

    private static class RemovalReasonHoldingNullCallback implements EntityInLevelCallback, RemovalReasonHolder {
        private final LivingEntity entity;
        private final EntityInLevelCallback callback;
        @Nullable
        private Entity.RemovalReason removalReason;

        private RemovalReasonHoldingNullCallback(LivingEntity entity, EntityInLevelCallback callback) {
            this.entity = entity;
            this.callback = callback;
        }

        @Override
        public void onMove() {
            this.callback.onMove();
        }

        @Override
        public void onRemove(Entity.RemovalReason reason) {
            this.removalReason = reason;
            ((EntityAccessor) this.entity).armorquickswap$callUnsetRemoved();
        }

        @Override
        public Entity.RemovalReason getRemovalReason() {
            return this.removalReason;
        }
    }

    private record ForwardingEntityInLevelCallback(EntityInLevelCallback callback,
                                                   RemovalReasonHolder holder) implements EntityInLevelCallback {

        @Override
        public void onMove() {
            this.callback.onMove();
        }

        @Override
        public void onRemove(Entity.RemovalReason reason) {
            if (reason == null) {
                if (this.holder.getRemovalReason() != null) {
                    reason = this.holder.getRemovalReason();
                } else {
                    reason = Entity.RemovalReason.KILLED;
                }
            }
            this.callback.onRemove(reason);
        }
    }
}
