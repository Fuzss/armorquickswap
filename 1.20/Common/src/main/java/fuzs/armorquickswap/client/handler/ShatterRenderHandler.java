package fuzs.armorquickswap.client.handler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.helper.EntityModelAdapter;
import fuzs.armorquickswap.mixin.client.accessor.AgeableListModelAccessor;
import fuzs.armorquickswap.mixin.client.accessor.LivingEntityRendererAccessor;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ShatterRenderHandler {
    private static final Map<LivingEntityRenderer<?, ?>, EntityModelAdapter> MODEL_ADAPTERS = new MapMaker().concurrencyLevel(1).weakKeys().makeMap();

    public static boolean containedInBlacklist(EntityType<?> entityType) {
        // TODO blacklist config option
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity, M extends EntityModel<T>> EventResult onBeforeRenderEntity(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {

        if (!entity.isDeadOrDying() || ShatterRenderHandler.containedInBlacklist(entity.getType())) {
            return EventResult.PASS;
        }

        // vanilla is very aggressive with syncing those shared flags, so we set them during rendering since they are used right after the entity is rendered
        // fire flag prevents rendering the fire overlay, mainly useful for undead mobs burning in the sun
        entity.setSharedFlagOnFire(false);
        // invisibility flag prevents the mob shadow from rendering which is not desired for the death animation
        // unfortunately the entity hitbox (F3+B) also no longer renders, but that's how it is
        entity.setInvisible(true);

        poseStack.pushPose();

        // same setup as vanilla living entity renderer
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        ((LivingEntityRendererAccessor<T, M>) renderer).armorquickswap$callScale(entity, poseStack, partialTick);
        poseStack.translate(0F, -1.501F, 0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.yBodyRotO));

        EntityModelAdapter modelPartRenderer = MODEL_ADAPTERS.computeIfAbsent(renderer, ShatterRenderHandler::createModelAdapter);
        modelPartRenderer.prepareModelParts(entity, partialTick);

        RenderType renderType = RenderType.entityTranslucentCull(renderer.getTextureLocation(entity));
        // vanilla seems to stop rendering for very low alpha, so we cannot fade out properly
        float animationProgress = Mth.clamp((entity.deathTime + partialTick) / ShatterTickHandler.SHATTER_DEATH_TIME, 0.0F, 1.0F);
        modelPartRenderer.setupAndRenderModelParts(entity, poseStack, multiBufferSource.getBuffer(renderType), packedLight, animationProgress);

        modelPartRenderer.finalizeModelParts();
        poseStack.popPose();

        return EventResult.INTERRUPT;
    }

    private static EntityModelAdapter createModelAdapter(LivingEntityRenderer<?, ?> renderer) {
        EntityModel<?> model = renderer.getModel();
        if (model instanceof HierarchicalModel<?> hierarchicalModel) {
            return new EntityModelAdapter(renderer, hierarchicalModel.root().getAllParts().distinct().collect(Collectors.toCollection(Sets::newIdentityHashSet)));
        } else if (model instanceof AgeableListModel<?> ageableListModel) {
            Iterable<ModelPart> bodyParts = ((AgeableListModelAccessor) ageableListModel).armorquickswap$callBodyParts();
            Iterable<ModelPart> headParts = ((AgeableListModelAccessor) ageableListModel).armorquickswap$callHeadParts();
            return new EntityModelAdapter.AgeableEntityModelAdapter(renderer, ImmutableSet.copyOf(bodyParts), ImmutableSet.copyOf(headParts));
        } else if (model instanceof RabbitModel<?>) {
            return new EntityModelAdapter(renderer, getAllModelParts(model)) {

                @Override
                public void setupAndRenderModelParts(LivingEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, float animationProgress) {
                    poseStack.pushPose();
                    poseStack.scale(0.6F, 0.6F, 0.6F);
                    poseStack.translate(0.0F, 1.0F, 0.0F);
                    super.setupAndRenderModelParts(entity, poseStack, vertexConsumer, packedLight, animationProgress);
                    poseStack.popPose();
                }
            };
        } else {
            return new EntityModelAdapter(renderer, getAllModelParts(model));
        }
    }

    private static Collection<ModelPart> getAllModelParts(EntityModel<?> model) {
        Set<ModelPart> modelParts = Sets.newIdentityHashSet();
        Class<?> clazz = model.getClass();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        while (clazz != EntityModel.class) {
            try {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getType() == ModelPart.class) {
                        field.setAccessible(true);
                        modelParts.add((ModelPart) lookup.unreflectGetter(field).invoke(model));
                    } else if (field.getType() == ModelPart[].class) {
                        field.setAccessible(true);
                        modelParts.addAll(Arrays.asList((ModelPart[]) lookup.unreflectGetter(field).invoke(model)));
                    }
                }
            } catch (Throwable e) {
                ArmorQuickSwap.LOGGER.warn("Unable to process model {}", model, e);
            }
            clazz = clazz.getSuperclass();
        }
        return modelParts.stream().flatMap(ModelPart::getAllParts).distinct().collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}
