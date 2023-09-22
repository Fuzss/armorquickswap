package fuzs.armorquickswap.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.network.client.ServerboundOpenCraftingGridMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class PortableCraftingHandler {
    public static final KeyMapping OPEN_CRAFTING_GRID_KEY_MAPPING = new KeyMapping("key.openCraftingGrid", InputConstants.KEY_G, "key.categories." + ArmorQuickSwap.MOD_ID);

    public static void onStartClientTick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player != null && !player.isSpectator()) {
            while (OPEN_CRAFTING_GRID_KEY_MAPPING.consumeClick()) {
                ArmorQuickSwap.NETWORK.sendToServer(new ServerboundOpenCraftingGridMessage());
            }
        }
    }
}
