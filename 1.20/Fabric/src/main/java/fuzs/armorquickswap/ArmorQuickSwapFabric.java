package fuzs.armorquickswap;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class ArmorQuickSwapFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
    }
}
