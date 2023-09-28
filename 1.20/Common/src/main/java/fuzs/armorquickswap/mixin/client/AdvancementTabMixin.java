package fuzs.armorquickswap.mixin.client;

import fuzs.armorquickswap.client.handler.AdvancementBackgroundType;
import fuzs.armorquickswap.client.handler.ClickableAdvancementsHandler;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementTab.class)
abstract class AdvancementTabMixin {
    @Shadow
    @Final
    private DisplayInfo display;
    @Shadow
    private double scrollX;
    @Shadow
    private double scrollY;

    @Redirect(method = "drawContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;enableScissor(IIII)V"))
    public void drawContents(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY) {

    }

    @Redirect(method = "drawContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;disableScissor()V"))
    public void drawContents(GuiGraphics guiGraphics) {

    }

    @Inject(method = "drawContents", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = At.Shift.AFTER))
    public void drawContents$0(GuiGraphics guiGraphics, int x, int y, CallbackInfo callback) {

        float currentScale = ClickableAdvancementsHandler.getCurrentScale();
        guiGraphics.pose().scale(currentScale, currentScale, 1.0F);
        guiGraphics.pose().translate(AdvancementsScreen.WINDOW_INSIDE_WIDTH * (1.0F - currentScale), AdvancementsScreen.WINDOW_INSIDE_HEIGHT * (1.0F - currentScale), 0.0F);
    }

    @Inject(method = "drawContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V", shift = At.Shift.BEFORE))
    public void drawContents$1(GuiGraphics guiGraphics, int x, int y, CallbackInfo callback) {

        float currentScale = ClickableAdvancementsHandler.getCurrentScale();
        int scrollX = Mth.floor(this.scrollX);
        int scrollY = Mth.floor(this.scrollY);
        int scrollXMod = scrollX % 16;
        int scrollYMod = scrollY % 16;

        int defaultPanelWidth = 17;
        double currentPanelWidth = Math.ceil(defaultPanelWidth / currentScale);
        int additionalPanelWidth = (int) Math.max(0, Math.ceil((currentPanelWidth - defaultPanelWidth) / 2.0));
        int defaultPanelHeight = 10;
        double currentPanelHeight = Math.ceil(defaultPanelHeight / currentScale);
        int additionalPanelHeight = (int) Math.max(0.0, Math.ceil((currentPanelHeight - defaultPanelHeight) / 2.0));

        for (int i = -1 - additionalPanelWidth; i <= 15 + additionalPanelWidth; ++i) {
            for (int j = -1 - additionalPanelHeight, k = (int) currentPanelHeight; j <= 8 + additionalPanelHeight; ++j, --k) {
                RandomSource random = ClickableAdvancementsHandler.getFreshRandomSource((7 * 31 + i) * 31 + j);
                AdvancementBackgroundType type = AdvancementBackgroundType.getBackgroundAtHeight(random, Mth.clamp((int) (k / currentPanelHeight * 100.0), 0 ,100));
                guiGraphics.blit(type.getBackground(), scrollXMod + 16 * i, scrollYMod + 16 * j, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
    }

    @Inject(method = "drawTooltips", at = @At("HEAD"), cancellable = true)
    public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int width, int height, CallbackInfo callback) {
        callback.cancel();
    }
}
