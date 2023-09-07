package fuzs.armorquickswap;

import fuzs.armorquickswap.data.ModParticleDescriptionProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(ArmorQuickSwap.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ArmorQuickSwapForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        if (ModLoaderEnvironment.INSTANCE.isServer()) return;
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent evt) {
        evt.getGenerator().addProvider(true, new ModParticleDescriptionProvider(evt, ArmorQuickSwap.MOD_ID));
    }
}
