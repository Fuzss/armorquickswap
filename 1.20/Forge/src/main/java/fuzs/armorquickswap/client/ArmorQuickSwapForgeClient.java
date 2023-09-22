package fuzs.armorquickswap.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.handler.ClickableAdvancementsHandler;
import fuzs.armorquickswap.client.handler.ToastControlHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = ArmorQuickSwap.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArmorQuickSwapForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwapClient::new);
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener((final ToastAddEvent evt) -> {
            Minecraft minecraft = Minecraft.getInstance();
            EventResult result = ToastControlHandler.onAddToast(minecraft.getToasts(), evt.getToast());
            if (result.isInterrupt()) evt.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((final ToastAddEvent evt) -> {
            Minecraft minecraft = Minecraft.getInstance();
            EventResult result = ClickableAdvancementsHandler.onAddToast(minecraft.getToasts(), evt.getToast());
            if (result.isInterrupt()) evt.setCanceled(true);
        });
    }
}
