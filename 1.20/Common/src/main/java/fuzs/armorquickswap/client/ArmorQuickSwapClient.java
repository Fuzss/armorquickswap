package fuzs.armorquickswap.client;

import fuzs.armorquickswap.client.handler.ArmorStandEquipmentHandler;
import fuzs.armorquickswap.client.handler.InventoryArmorClickHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.InteractionInputEvents;
import fuzs.puzzleslib.api.client.event.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class ArmorQuickSwapClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        InteractionInputEvents.USE.register(ArmorStandEquipmentHandler::onUseInteraction);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(InventoryArmorClickHandler::onBeforeMouseClick);
    }
}
