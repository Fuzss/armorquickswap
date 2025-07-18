package fuzs.armorquickswap.neoforge.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.ArmorQuickSwapClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ArmorQuickSwap.MOD_ID, dist = Dist.CLIENT)
public class ArmorQuickSwapNeoForgeClient {

    public ArmorQuickSwapNeoForgeClient() {
        ClientModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwapClient::new);
    }
}
