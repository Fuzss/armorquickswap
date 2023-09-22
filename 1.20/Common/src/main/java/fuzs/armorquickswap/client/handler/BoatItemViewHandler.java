package fuzs.armorquickswap.client.handler;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.armorquickswap.mixin.client.accessor.ItemInHandRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class BoatItemViewHandler {
    private static ItemStack lastMainHandItem;
    private static ItemStack lastOffHandItem;

    public static void onStartClientTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            ItemInHandRenderer itemInHandRenderer = minecraft.gameRenderer.itemInHandRenderer;
            lastMainHandItem = ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$getMainHandItem();
            lastOffHandItem = ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$getOffHandItem();
        }
    }

    public static void onEndClientTick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player != null && player.isHandsBusy()) {
            ItemInHandRenderer itemInHandRenderer = minecraft.gameRenderer.itemInHandRenderer;
            if (lastMainHandItem.is(ModRegistry.HOLDABLE_WHILE_ROWING_ITEM_TAG)) {
                float mainHandHeight = ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$getOMainHandHeight();
                ItemStack mainHandItem = lastMainHandItem;
                ItemStack currentMainHandItem = player.getMainHandItem();
                if (ItemStack.matches(mainHandItem, currentMainHandItem)) {
                    mainHandItem = currentMainHandItem;
                }
                float attackStrengthScale = player.getAttackStrengthScale(1.0F);
                mainHandHeight += Mth.clamp((mainHandItem == currentMainHandItem ? attackStrengthScale * attackStrengthScale * attackStrengthScale : 0.0F) - mainHandHeight, -0.4F, 0.4F);
                if (mainHandHeight < 0.1F) {
                    mainHandItem = currentMainHandItem;
                }
                ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$setMainHandHeight(mainHandHeight);
                ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$setMainHandItem(mainHandItem);
            }
            if (lastOffHandItem.is(ModRegistry.HOLDABLE_WHILE_ROWING_ITEM_TAG)) {
                float offHandHeight = ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$getOOffHandHeight();
                ItemStack offHandItem = lastOffHandItem;
                ItemStack currentOffHandItem = player.getOffhandItem();
                if (ItemStack.matches(offHandItem, currentOffHandItem)) {
                    offHandItem = currentOffHandItem;
                }
                offHandHeight += Mth.clamp((offHandItem == currentOffHandItem ? 1.0F : 0.0F) - offHandHeight, -0.4F, 0.4F);
                if (offHandHeight < 0.1F) {
                    offHandItem = currentOffHandItem;
                }
                ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$setOffHandHeight(offHandHeight);
                ((ItemInHandRendererAccessor) itemInHandRenderer).armorquickswap$setOffHandItem(offHandItem);
            }
        }
    }
}
