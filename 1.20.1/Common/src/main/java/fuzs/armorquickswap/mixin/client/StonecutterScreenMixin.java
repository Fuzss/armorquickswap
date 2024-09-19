package fuzs.armorquickswap.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(StonecutterScreen.class)
abstract class StonecutterScreenMixin extends AbstractContainerScreen<StonecutterMenu> {

    public StonecutterScreenMixin(StonecutterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @ModifyConstant(method = {"renderTooltip", "renderButtons", "renderRecipes", "mouseClicked", "mouseDragged", "mouseScrolled", "getOffscreenRows"}, constant = @Constant(intValue = 4))
    public int getRecipesColumns(int recipesColumns) {
        return 7;
    }

    @ModifyConstant(method = {"renderBg", "renderTooltip", "mouseClicked"}, constant = @Constant(intValue = 52))
    public int getRecipesX(int recipesX) {
        return 40;
    }

    @ModifyConstant(method = {"renderBg", "mouseClicked"}, constant = @Constant(intValue = 119))
    public int getScrollerX(int scrollerX) {
        return 155;
    }

    @ModifyConstant(method = {"renderTooltip", "isScrollBarActive"}, constant = @Constant(intValue = 12))
    public int getMaxRecipes$0(int maxRecipes) {
        return 21;
    }

    @ModifyConstant(method = {"mouseClicked"}, constant = @Constant(intValue = 12), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryButtonClick(II)V")))
    public int getMaxRecipes$1(int maxRecipes) {
        return 21;
    }

    @ModifyConstant(method = {"renderBg"}, constant = @Constant(intValue = 12), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/StonecutterScreen;startIndex:I")))
    public int getMaxRecipes$2(int maxRecipes) {
        return 21;
    }
}
