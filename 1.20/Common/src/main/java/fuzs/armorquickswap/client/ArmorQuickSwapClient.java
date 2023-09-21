package fuzs.armorquickswap.client;

import fuzs.armorquickswap.client.handler.*;
import fuzs.armorquickswap.client.init.BloodParticle;
import fuzs.armorquickswap.client.init.ClientModRegistry;
import fuzs.armorquickswap.init.ModRegistry;
import fuzs.armorquickswap.mixin.client.accessor.LivingEntityAccessor;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.ParticleProvidersContext;
import fuzs.puzzleslib.api.client.event.v1.InteractionInputEvents;
import fuzs.puzzleslib.api.client.event.v1.RenderLivingEvents;
import fuzs.puzzleslib.api.client.event.v1.ScreenMouseEvents;
import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.event.v1.LoadCompleteCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDeathCallback;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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

        ParticleStatus particleStatus = ParticleStatus.DECREASED;
        boolean[] invisible = new boolean[1];
        PlayerTickEvents.START.register(player -> {
            if (player instanceof LocalPlayer) {
                switch (particleStatus) {
                    case MINIMAL:
                        invisible[0] = player.isInvisible();
                        player.setInvisible(true);
                    case DECREASED:
                        player.getEntityData().set(LivingEntityAccessor.armorquickswap$getdataEffectAmbienceId(), true);
                }
            }
        });
        PlayerTickEvents.END.register(player -> {
            if (player instanceof LocalPlayer && player.isInvisible() && particleStatus == ParticleStatus.MINIMAL) {
                player.setInvisible(invisible[0]);
            }
        });

        InteractionInputEvents.USE.register((minecraft, player, interactionHand, hitResult) -> {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState state = minecraft.level.getBlockState(pos);
                if (state.is(ModRegistry.CLICK_THROUGH_BLOCK_TAG) && state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
                    pos = pos.relative(direction.getOpposite());
                    if (!useItemOnMenuProvider(minecraft, player, direction, pos) && useItem(minecraft, player, interactionHand, (BlockHitResult) hitResult)) {
                        return EventResult.INTERRUPT;
                    }
                }
            } else if (!player.isSecondaryUseActive() && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity.getType().is(ModRegistry.CLICK_THROUGH_ENTITY_TYPE_TAG)) {
                    BlockPos pos = entity.blockPosition().relative(entity.getDirection().getOpposite());
                    useItemOnMenuProvider(minecraft, player, entity.getDirection(), pos);
                }
            }
            return EventResult.PASS;
        });

        LoadCompleteCallback.EVENT.register(() -> {
            Minecraft.getInstance().options.tutorialStep = TutorialSteps.NONE;
        });

        ScreenMouseEvents.beforeMouseClick(ChatScreen.class).register(ClickableAdvancementsHandler::onBeforeMouseClick);
    }

    private static boolean useItemOnMenuProvider(Minecraft minecraft, LocalPlayer player, Direction direction, BlockPos pos) {
        if (minecraft.level.getBlockState(pos).getMenuProvider(minecraft.level, pos) != null) {
            if (!player.isSecondaryUseActive()) {
                Vec3 hitLocation = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                minecraft.hitResult = new BlockHitResult(hitLocation, direction, pos, false);
                return true;
            }
            return false;
        }
        return true;
    }

    private static boolean useItem(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() instanceof SignApplicator) {
            int itemCount = itemInHand.getCount();
            InteractionResult result = useItemWithoutSecondaryUse(minecraft, player, interactionHand, hitResult);
            if (result.consumesAction() && result.shouldSwing()) {
                player.swing(interactionHand);
                if (!itemInHand.isEmpty() && (itemInHand.getCount() != itemCount || minecraft.gameMode.hasInfiniteItems())) {
                    minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static InteractionResult useItemWithoutSecondaryUse(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, BlockHitResult hitResult) {
        boolean shiftKeyDown = player.input.shiftKeyDown;
        player.input.shiftKeyDown = false;
        player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        InteractionResult result = minecraft.gameMode.useItemOn(player, interactionHand, hitResult);
        player.input.shiftKeyDown = shiftKeyDown;
        player.connection.send(new ServerboundPlayerCommandPacket(player, shiftKeyDown ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        return result;
    }

    @Override
    public void onRegisterParticleProviders(ParticleProvidersContext context) {
        context.registerClientParticleProvider(ClientModRegistry.BLOOD_PARTICLE_TYPE, BloodParticle.Provider::new);
    }

    @Override
    public ContentRegistrationFlags[] getContentRegistrationFlags() {
        return new ContentRegistrationFlags[]{ContentRegistrationFlags.CLIENT_PARTICLE_TYPES};
    }
}
