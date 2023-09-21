package fuzs.armorquickswap.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;

public class BoatItemViewHandler {

    public static void onStartClientTick(Minecraft minecraft) {
        ItemInHandRenderer itemInHandRenderer = minecraft.gameRenderer.itemInHandRenderer;
    }

    public static void onEndClientTick(Minecraft minecraft) {

    }
}
