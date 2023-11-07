package fuzs.armorquickswap.client.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fuzs.armorquickswap.capability.FireOverlayCapability;
import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.init.v3.tags.TypedTagFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColoredFireOverlayHandler {
    private static final Map<ResourceLocation, Block> FIRE_BLOCKS;
    private static final Set<Block> PROCESSED_BLOCKS = Sets.newIdentityHashSet();
    private static final Map<Block, FireOverlayTexture> FIRE_OVERLAY_TEXTURES = Maps.newIdentityHashMap();

    static {
        Map<ResourceLocation, Block> blocks = Maps.newHashMap();
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            if (entry.getValue() instanceof BaseFireBlock) blocks.put(entry.getKey().location(), entry.getValue());
        }
        FIRE_BLOCKS = ImmutableMap.copyOf(blocks);
    }

    public static TagKey<Block> getBlockTag(Block block) {
        return TypedTagFactory.BLOCK.make(BuiltInRegistries.BLOCK.getKey(block).withSuffix("_sources"));
    }

    public static TagKey<Fluid> getFluidTag(Block block) {
        return TypedTagFactory.FLUID.make(BuiltInRegistries.BLOCK.getKey(block).withSuffix("_sources"));
    }

    public static EventResult onLivingAttack(LivingEntity entity, DamageSource source, float amount) {
        isInFire(entity, source);
        return EventResult.PASS;
    }

    public static void isInFire(Entity entity, DamageSource source) {
        if (entity.level().isClientSide && source.is(DamageTypes.IN_FIRE)) {
            List<Map.Entry<BlockPos, BlockState>> insideBlocks = getInsideBlocks(entity);
            for (Map.Entry<BlockPos, BlockState> entry : insideBlocks) {
                if (FIRE_OVERLAY_TEXTURES.containsKey(entry.getValue().getBlock())) {
                    ModRegistry.FIRE_OVERLAY_CAPABILITY.get(entity).setFireBlock(entry.getValue().getBlock());
                    break;
                }
            }
        }
    }

    private static List<Map.Entry<BlockPos, BlockState>> getInsideBlocks(Entity entity) {

        List<Map.Entry<BlockPos, BlockState>> blocks = Lists.newArrayList();

        AABB aabb = entity.getBoundingBox();
        BlockPos blockpos = BlockPos.containing(aabb.minX + 1.0E-7D, aabb.minY + 1.0E-7D, aabb.minZ + 1.0E-7D);
        BlockPos blockpos1 = BlockPos.containing(aabb.maxX - 1.0E-7D, aabb.maxY - 1.0E-7D, aabb.maxZ - 1.0E-7D);
        if (entity.level().hasChunksAt(blockpos, blockpos1)) {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for(int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
                for(int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
                    for(int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                        blockpos$mutableblockpos.set(i, j, k);
                        BlockState blockstate = entity.level().getBlockState(blockpos$mutableblockpos);
                        blocks.add(Map.entry(blockpos$mutableblockpos.immutable(), blockstate));
                    }
                }
            }
        }

        blocks.sort(Comparator.comparingDouble(entry -> {
            return entry.getKey().distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ());
        }));

        return blocks;
    }

    public static EventResultHolder<UnbakedModel> onModifyUnbakedModel(ResourceLocation modelLocation, Supplier<UnbakedModel> unbakedModel, Function<ResourceLocation, UnbakedModel> modelGetter, BiConsumer<ResourceLocation, UnbakedModel> modelAdder) {
        ResourceLocation resourceLocation = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath());
        Block block = FIRE_BLOCKS.get(resourceLocation);
        if (block != null && !PROCESSED_BLOCKS.contains(block)) {
            UnbakedModel model = unbakedModel.get();
            List<Material> materials = Lists.newArrayList();
            $1: if (model instanceof MultiPart multiPart) {
                for (Selector selector : multiPart.getSelectors()) {
                    for (Variant variant : selector.getVariant().getVariants()) {
                        UnbakedModel variantModel = modelGetter.apply(variant.getModelLocation());
                        if (variantModel instanceof BlockModel blockModel) {
                            if (!blockModel.isResolved()) blockModel.resolveParents(modelGetter);
                            for (BlockElement element : blockModel.getElements()) {
                                for (BlockElementFace face : element.faces.values()) {
                                    Material material = blockModel.getMaterial(face.texture);
                                    if (!MissingTextureAtlasSprite.getLocation().equals(material.texture())) {
                                        materials.add(material);
                                        if (materials.size() == 2) {
                                            break $1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FireOverlayTexture fireOverlayTexture = FireOverlayTexture.from(materials);
            if (fireOverlayTexture != null) {
                FIRE_OVERLAY_TEXTURES.put(block, fireOverlayTexture);
            }
        }
        return EventResultHolder.pass();
    }

    public static void onAfterModelLoading(Supplier<ModelManager> modelManager) {
        PROCESSED_BLOCKS.clear();
    }

    public static EventResult onRenderBlockOverlay(LocalPlayer player, PoseStack poseStack, @Nullable BlockState blockState) {
        if (blockState != null && blockState.is(Blocks.FIRE)) {
            Block block = ModRegistry.FIRE_OVERLAY_CAPABILITY.get(player).getFireBlock();
            if (block != null) {
                FireOverlayTexture fireOverlayTexture = FIRE_OVERLAY_TEXTURES.get(block);
                if (fireOverlayTexture != null) {
                    renderFire(Minecraft.getInstance(), poseStack, fireOverlayTexture.fire1());
                    return EventResult.INTERRUPT;
                }
            }
        }
        return EventResult.PASS;
    }

    private static void renderFire(Minecraft minecraft, PoseStack poseStack, Material material) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        TextureAtlasSprite textureAtlasSprite = material.sprite();
        RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
        float f = textureAtlasSprite.getU0();
        float g = textureAtlasSprite.getU1();
        float h = (f + g) / 2.0F;
        float i = textureAtlasSprite.getV0();
        float j = textureAtlasSprite.getV1();
        float k = (i + j) / 2.0F;
        float l = textureAtlasSprite.uvShrinkRatio();
        float m = Mth.lerp(l, f, h);
        float n = Mth.lerp(l, g, h);
        float o = Mth.lerp(l, i, k);
        float p = Mth.lerp(l, j, k);
        float q = 1.0F;

        for(int r = 0; r < 2; ++r) {
            poseStack.pushPose();
            float s = -0.5F;
            float t = 0.5F;
            float u = -0.5F;
            float v = 0.5F;
            float w = -0.5F;
            poseStack.translate((float)(-(r * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees((float)(r * 2 - 1) * 10.0F));
            Matrix4f matrix4f = poseStack.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            bufferBuilder.vertex(matrix4f, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(n, p).endVertex();
            bufferBuilder.vertex(matrix4f, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(m, p).endVertex();
            bufferBuilder.vertex(matrix4f, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(m, o).endVertex();
            bufferBuilder.vertex(matrix4f, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(n, o).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
            poseStack.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }

    public static TextureAtlasSprite getFireOverlaySprite(Entity entity, int fireIndex, TextureAtlasSprite fallback) {
        Optional<FireOverlayCapability> optional = ModRegistry.FIRE_OVERLAY_CAPABILITY.maybeGet(entity);
        if (optional.isPresent() && optional.get().getFireBlock() != null) {
            ColoredFireOverlayHandler.FireOverlayTexture fireOverlayTexture = ColoredFireOverlayHandler.FIRE_OVERLAY_TEXTURES.get(optional.get().getFireBlock());
            if (fireOverlayTexture != null) {
                return fireOverlayTexture.fire().get(fireIndex).sprite();
            }
        }
        return fallback;
    }

    private record FireOverlayTexture(List<Material> fire) {

        public FireOverlayTexture(Material fire0, Material fire1) {
            this(List.of(fire0, fire1));
            Objects.requireNonNull(fire0, "fire0 is null");
            Objects.requireNonNull(fire1, "fire1 is null");
        }

        public Material fire0() {
            return this.fire.get(0);
        }

        public Material fire1() {
            return this.fire.get(1);
        }

        @Nullable
        public static FireOverlayTexture from(List<Material> materials) {
            if (materials.size() == 1) {
                return new FireOverlayTexture(materials.get(0), materials.get(1));
            } else if (materials.size() == 2) {
                Material fire0 = materials.get(0);
                Material fire1 = materials.get(1);
                if (fire0.texture().getPath().contains("1") && !fire1.texture().getPath().contains("1") || fire1.texture().getPath().contains("0") && !fire0.texture().getPath().contains("0")) {
                    fire0 = materials.get(1);
                    fire1 = materials.get(0);
                }
                return new FireOverlayTexture(fire0, fire1);
            }
            return null;
        }
    }
}
