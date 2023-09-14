package fuzs.armorquickswap.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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

    @ModifyConstant(method = {"isScrollBarActive"}, constant = @Constant(intValue = 12))
    public int getMaxRecipes(int maxRecipes) {
        return 21;
    }
}
