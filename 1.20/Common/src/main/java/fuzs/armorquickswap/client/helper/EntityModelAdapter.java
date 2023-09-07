package fuzs.armorquickswap.client.helper;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fuzs.armorquickswap.mixin.client.accessor.AgeableListModelAccessor;
import fuzs.armorquickswap.mixin.client.accessor.LivingEntityRendererAccessor;
import fuzs.armorquickswap.mixin.client.accessor.ModelPartAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityModelAdapter {
    private final LivingEntityRenderer<?, ?> renderer;
    private final Map<ModelPart, PartPose> modelParts;

    public EntityModelAdapter(LivingEntityRenderer<?, ?> renderer, Collection<ModelPart> modelParts) {
        this.renderer = renderer;
        // store current poses for all model parts; a lot of models do not set every model property for the currently rendering entity,
        // meaning we are likely to change a value which is not going to be reset for the next actual entity render
        // all the original poses are restored at the end of our operation
        this.modelParts = storeModelPartPoses(modelParts);
    }

    protected static IdentityHashMap<ModelPart, PartPose> storeModelPartPoses(Collection<ModelPart> modelParts) {
        return modelParts.stream().collect(Collectors.toMap(Function.identity(), ModelPart::storePose, (o1, o2) -> o1, Maps::newIdentityHashMap));
    }

    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, M extends EntityModel<T>> void prepareModelParts(T entity, float partialTick) {
        // we need to call these methods as they also handle things like model part visibility
        // we don't care about the rotations, those are reset directly afterward
        float yRotDiff = entity.yHeadRotO - entity.yBodyRotO;
        float bob = ((LivingEntityRendererAccessor<T, M>) this.renderer).armorquickswap$callGetBob(entity, partialTick);
        ((M) this.renderer.getModel()).prepareMobModel(entity, 0.0F, 0.0F, partialTick);
        ((M) this.renderer.getModel()).setupAnim(entity, 0.0F, 0.0F, bob, yRotDiff, entity.xRotO);
        this.modelParts.keySet().forEach(ModelPart::resetPose);
    }

    public void setupAndRenderModelParts(LivingEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, float animationProgress) {

        setupAndRenderModelParts(this.modelParts.keySet(), entity, poseStack, vertexConsumer, packedLight, animationProgress);
    }

    protected static void setupAndRenderModelParts(Collection<ModelPart> modelParts, LivingEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, float animationProgress) {

        ModelPart[] parts = explodeModelParts(modelParts).toArray(ModelPart[]::new);
        Vec3 deltaMovement = ClientEntityData.getDeltaMovement(entity);
        float alpha = 1.0F - animationProgress;
        RandomSource random = entity.getRandom();

        for (int i = 0; i < parts.length; i++) {

            poseStack.pushPose();

            random.setSeed((long) random.nextInt() * entity.getId() * i * 1000);
            setupRotations(poseStack, animationProgress, deltaMovement, random);

            ModelPart modelPart = parts[i];
            modelPart.translateAndRotate(poseStack);
            ModelPartAccessor.class.cast(modelPart).armorquickswap$callCompile(poseStack.last(), vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);

            poseStack.popPose();
        }
    }

    private static Collection<ModelPart> explodeModelParts(Collection<ModelPart> parts) {

        // it is important that parents generally come before children,
        // which should work fine by using identity set since the order is related to the initialization order of the instances (?)
        // ...well at least it does work in-game and this seems a valid reason why it does haha
        Set<ModelPart> modelParts = Sets.newIdentityHashSet();
        parts.forEach(part -> explodeModelPartAndChildren(part, modelParts));
        modelParts.forEach(modelPart -> modelPart.yRot -= 12.0);

        return modelParts;
    }

    private static void explodeModelPartAndChildren(ModelPart modelPart, Collection<ModelPart> modelParts) {

        if (!modelPart.visible || !modelParts.add(modelPart)) return;

        for (ModelPart child : ModelPartAccessor.class.cast(modelPart).armorquickswap$getChildren().values()) {
            child.x += modelPart.x;
            child.y += modelPart.y;
            child.z += modelPart.z;
            child.xRot += modelPart.xRot;
            child.yRot += modelPart.yRot;
            child.zRot += modelPart.zRot;
            explodeModelPartAndChildren(child, modelParts);
        }
    }

    private static void setupRotations(PoseStack poseStack, float animationProgress, Vec3 deltaMovement, RandomSource random) {

        double offsetX = random.nextFloat() * (random.nextFloat() > 0.5F ? -1.0 : 1) * animationProgress * deltaMovement.z * 5.0;
        double offsetY = random.nextDouble() * animationProgress * (deltaMovement.y + (random.nextDouble() - 1.0));
        double offsetZ = random.nextFloat() * (random.nextFloat() > 0.5F ? -1.0 : 1.0) * animationProgress * deltaMovement.x * 5.0;
        poseStack.translate(offsetX, offsetY, offsetZ);

        float rotationBase = 180.0F * random.nextFloat() * animationProgress;
        float rotationX = random.nextFloat() * (random.nextBoolean() ? -1.0F : 1.0F) * animationProgress;
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationBase * rotationX));
        float rotationY = random.nextFloat() * (random.nextBoolean() ? -1.0F : 1.0F) * animationProgress;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationBase * rotationY));
        float rotationZ = random.nextFloat() * (random.nextBoolean() ? -1.0F : 1.0F) * animationProgress;
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationBase * rotationZ));
    }

    public void finalizeModelParts() {
        // restore original poses for reasons mentioned above
        this.modelParts.forEach(ModelPart::loadPose);
    }

    public static class AgeableEntityModelAdapter extends EntityModelAdapter {
        private final Map<ModelPart, PartPose> headParts;
        private final boolean scaleHead;
        private final float babyYHeadOffset;
        private final float babyZHeadOffset;
        private final float babyHeadScale;
        private final float babyBodyScale;
        private final float bodyYOffset;

        public AgeableEntityModelAdapter(LivingEntityRenderer<?, ?> renderer, Collection<ModelPart> bodyParts, Collection<ModelPart> headParts) {
            super(renderer, bodyParts);
            this.headParts = storeModelPartPoses(headParts);
            AgeableListModelAccessor accessor = (AgeableListModelAccessor) renderer.getModel();
            this.scaleHead = accessor.armorquickswap$getScaleHead();
            this.babyYHeadOffset = accessor.armorquickswap$getBabyYHeadOffset();
            this.babyZHeadOffset = accessor.armorquickswap$getBabyZHeadOffset();
            this.babyHeadScale = accessor.armorquickswap$getBabyHeadScale();
            this.babyBodyScale = accessor.armorquickswap$getBabyBodyScale();
            this.bodyYOffset = accessor.armorquickswap$getBodyYOffset();
        }

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void prepareModelParts(T entity, float partialTick) {
            super.prepareModelParts(entity, partialTick);
            this.headParts.keySet().forEach(ModelPart::resetPose);
        }

        @Override
        public void setupAndRenderModelParts(LivingEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, float animationProgress) {

            if (entity.isBaby()) {

                poseStack.pushPose();

                if (this.scaleHead) {
                    float scale = 1.5F / this.babyHeadScale;
                    poseStack.scale(scale, scale, scale);
                }

                poseStack.translate(0.0F, this.babyYHeadOffset / 16.0F, this.babyZHeadOffset / 16.0F);
                setupAndRenderModelParts(this.headParts.keySet(), entity, poseStack, vertexConsumer, packedLight, animationProgress);

                poseStack.popPose();

                poseStack.pushPose();

                float scale = 1.0F / this.babyBodyScale;
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
                super.setupAndRenderModelParts(entity, poseStack, vertexConsumer, packedLight, animationProgress);

                poseStack.popPose();
            } else {

                setupAndRenderModelParts(this.headParts.keySet(), entity, poseStack, vertexConsumer, packedLight, animationProgress);
                super.setupAndRenderModelParts(entity, poseStack, vertexConsumer, packedLight, animationProgress);
            }
        }

        @Override
        public void finalizeModelParts() {
            super.finalizeModelParts();
            this.headParts.forEach(ModelPart::loadPose);
        }
    }
}
