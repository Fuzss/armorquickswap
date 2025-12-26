package fuzs.armorquickswap.neoforge;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.neoforged.fml.common.Mod;

@Mod(ArmorQuickSwap.MOD_ID)
public class ArmorQuickSwapNeoForge {

    public ArmorQuickSwapNeoForge() {
        // This is for testing the client-only functionality in a development environment.
        if (!ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment() || ModLoaderEnvironment.INSTANCE.isClient()) {
            ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
        }
    }
}
