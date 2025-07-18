package fuzs.armorquickswap.fabric.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.ArmorQuickSwapClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class ArmorQuickSwapFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwapClient::new);
    }
}
