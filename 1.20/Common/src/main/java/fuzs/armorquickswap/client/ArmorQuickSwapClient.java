package fuzs.armorquickswap.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.handler.ClickableAdvancementsHandler;
import fuzs.armorquickswap.client.handler.InventoryArmorClickHandler;
import fuzs.armorquickswap.client.handler.LocalArmorStandGearHandler;
import fuzs.puzzlesaccessapi.api.client.data.v2.BlockModelBuilder;
import fuzs.puzzlesaccessapi.api.client.data.v2.ItemModelBuilder;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.client.event.v1.*;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.level.block.Blocks;

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

        AddToastCallback.EVENT.register(ClickableAdvancementsHandler::onAddToast);
        ScreenMouseEvents.beforeMouseClick(ChatScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseClick);
        ScreenMouseEvents.beforeMouseClick(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseClick);
        ScreenMouseEvents.afterMouseClick(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onAfterMouseClick);
        ClientPlayerEvents.LOGGED_IN.register(ClickableAdvancementsHandler::onLoggedIn);
        ScreenEvents.afterRender(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onAfterRender);
        ScreenMouseEvents.beforeMouseScroll(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseScroll);
        ScreenEvents.afterInit(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onAfterInit);
        ScreenMouseEvents.beforeMouseDrag(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseDrag);
    }

    @Override
    public void onAddResourcePackFinders(PackRepositorySourcesContext context) {
        context.addRepositorySource(PackResourcesHelper.buildClientPack(ArmorQuickSwap.id("dynamic_models"), DynamicPackResources.create(dataProviderContext -> {
            return new AbstractModelProvider(dataProviderContext) {

                @Override
                public void addBlockModels(BlockModelBuilder builder) {
                    builder.copyModel(Blocks.DIAMOND_BLOCK, Blocks.COAL_BLOCK);
                    builder.copyModel(Blocks.COAL_BLOCK, Blocks.DIAMOND_BLOCK);
                }

                @Override
                public void addItemModels(ItemModelBuilder builder) {

                }
            };
        }), false));
    }
}
