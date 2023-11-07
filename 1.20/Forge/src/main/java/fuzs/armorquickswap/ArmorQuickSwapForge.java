package fuzs.armorquickswap;

import fuzs.armorquickswap.capability.FireOverlayCapability;
import fuzs.armorquickswap.data.ModBlockTagsProvider;
import fuzs.armorquickswap.data.ModEntityTypeTagsProvider;
import fuzs.armorquickswap.data.ModItemTagsProvider;
import fuzs.armorquickswap.data.ModParticleDescriptionProvider;
import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.capability.v2.ForgeCapabilityHelper;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.data.v2.core.DataProviderHelper;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(ArmorQuickSwap.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ArmorQuickSwapForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
        ForgeCapabilityHelper.setCapabilityToken(ModRegistry.FIRE_OVERLAY_CAPABILITY, new CapabilityToken<>() {
            // NO-OP
        });
        DataProviderHelper.registerDataProviders(ArmorQuickSwap.MOD_ID, ModBlockTagsProvider::new, ModEntityTypeTagsProvider::new, ModItemTagsProvider::new, ModParticleDescriptionProvider::new);
    }
}
