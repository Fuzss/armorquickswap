package fuzs.armorquickswap.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.handler.ClickableAdvancementsHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
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
            Toast toast = evt.getToast();
            if (toast instanceof RecipeToast || toast instanceof TutorialToast) {
                evt.setCanceled(true);
            }
            ClickableAdvancementsHandler.onAddToast(toast);
        });
    }
}
