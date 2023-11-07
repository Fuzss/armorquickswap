package fuzs.armorquickswap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.armorquickswap.client.handler.ColoredFireOverlayHandler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
abstract class EntityRenderDispatcherMixin {

    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private TextureAtlasSprite renderFlame$0(TextureAtlasSprite fire0, PoseStack matrixStack, MultiBufferSource buffer, Entity entity) {
        return ColoredFireOverlayHandler.getFireOverlaySprite(entity, 0, fire0);
    }

    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private TextureAtlasSprite renderFlame$1(TextureAtlasSprite fire1, PoseStack matrixStack, MultiBufferSource buffer, Entity entity) {
        return ColoredFireOverlayHandler.getFireOverlaySprite(entity, 1, fire1);
    }
}
