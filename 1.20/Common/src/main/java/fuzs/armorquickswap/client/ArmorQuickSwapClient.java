package fuzs.armorquickswap.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.client.handler.*;
import fuzs.armorquickswap.client.init.BloodParticle;
import fuzs.armorquickswap.client.init.ClientModRegistry;
import fuzs.puzzlesaccessapi.api.client.data.v2.BlockModelBuilder;
import fuzs.puzzlesaccessapi.api.client.data.v2.ItemModelBuilder;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.core.v1.context.ParticleProvidersContext;
import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.client.event.v1.*;
import fuzs.puzzleslib.api.client.screen.v2.KeyMappingActivationHelper;
import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.LoadCompleteCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDeathCallback;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerTickEvents;
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

        LivingDeathCallback.EVENT.register(ShatterTickHandler::onLivingDeath);
        LivingEvents.TICK.register(ShatterTickHandler::onLivingTick);
        RenderLivingEvents.BEFORE.register(ShatterRenderHandler::onBeforeRenderEntity);

        LivingDeathCallback.EVENT.register(MobDismembermentHandler::onLivingDeath);

        PlayerTickEvents.START.register(FewerPotionParticlesHandler::onStartPlayerTick);
        PlayerTickEvents.END.register(FewerPotionParticlesHandler::onEndPlayerTick);

        InteractionInputEvents.USE.register(ClickThroughHandler::onUseInteraction);

        LoadCompleteCallback.EVENT.register(ToastControlHandler::onLoadComplete);
        AddToastCallback.EVENT.register(ToastControlHandler::onAddToast);

        AddToastCallback.EVENT.register(ClickableAdvancementsHandler::onAddToast);
        ScreenMouseEvents.beforeMouseClick(ChatScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseClick);
        ScreenMouseEvents.beforeMouseClick(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseClick);
        ScreenMouseEvents.afterMouseClick(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onAfterMouseClick);
        ClientPlayerEvents.LOGGED_IN.register(ClickableAdvancementsHandler::onLoggedIn);
        ScreenEvents.afterRender(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onAfterRender);
        ScreenMouseEvents.beforeMouseScroll(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseScroll);
        ScreenEvents.afterInit(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onAfterInit);
        ScreenMouseEvents.beforeMouseDrag(AdvancementsScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseDrag);

        ClientTickEvents.START.register(PortableCraftingHandler::onStartClientTick);

        ClientTickEvents.START.register(BoatItemViewHandler::onStartClientTick);
        ClientTickEvents.END.register(BoatItemViewHandler::onEndClientTick);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(PortableCraftingHandler.OPEN_CRAFTING_GRID_KEY_MAPPING, KeyMappingActivationHelper.KeyActivationContext.GAME);
    }

    @Override
    public void onRegisterParticleProviders(ParticleProvidersContext context) {
        context.registerClientParticleProvider(ClientModRegistry.BLOOD_PARTICLE_TYPE, BloodParticle.Provider::new);
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

    @Override
    public ContentRegistrationFlags[] getContentRegistrationFlags() {
        return new ContentRegistrationFlags[]{ContentRegistrationFlags.CLIENT_PARTICLE_TYPES};
    }
}
