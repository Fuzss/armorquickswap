package fuzs.armorquickswap.neoforge;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(ArmorQuickSwap.MOD_ID)
public class ArmorQuickSwapNeoForge {

    public ArmorQuickSwapNeoForge() {
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
    }
}
