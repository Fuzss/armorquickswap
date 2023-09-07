package fuzs.armorquickswap.client;

import fuzs.armorquickswap.client.handler.InventoryArmorClickHandler;
import fuzs.armorquickswap.client.handler.LocalArmorStandGearHandler;
import fuzs.armorquickswap.client.handler.ShatterRenderHandler;
import fuzs.armorquickswap.client.handler.ShatterTickHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.InteractionInputEvents;
import fuzs.puzzleslib.api.client.event.v1.RenderLivingEvents;
import fuzs.puzzleslib.api.client.event.v1.ScreenMouseEvents;
import fuzs.puzzleslib.api.core.v1.context.AddReloadListenersContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDeathCallback;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Unit;

public class ArmorQuickSwapClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        // run before other mods like Quark that might interfere here
        // event phase must match PlayerInteractEvents#USE_ENTITY_AT as both use same underlying event on Fabric
        InteractionInputEvents.USE.register(EventPhase.BEFORE, LocalArmorStandGearHandler::onUseInteraction);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(InventoryArmorClickHandler::onBeforeMouseClick);

        LivingDeathCallback.EVENT.register(ShatterTickHandler::onLivingDeath);
        LivingEvents.TICK.register(ShatterTickHandler::onLivingTick);
        RenderLivingEvents.BEFORE.register(ShatterRenderHandler::onBeforeRenderEntity);
    }

    @Override
    public void onRegisterResourcePackReloadListeners(AddReloadListenersContext context) {
        context.registerReloadListener("gather_supported_entity_types", (preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
            return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(ShatterRenderHandler::onResourceManagerReload, gameExecutor);
        });
    }
}
