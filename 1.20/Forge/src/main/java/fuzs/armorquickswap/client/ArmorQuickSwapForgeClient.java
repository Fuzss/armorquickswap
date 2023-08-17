package fuzs.armorquickswap.client;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = ArmorQuickSwap.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArmorQuickSwapForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwapClient::new);
        registerHandlers();
    }

    private static void registerHandlers() {

        MinecraftForge.EVENT_BUS.addListener((final InputEvent.InteractionKeyMappingTriggered evt) -> {

            Minecraft minecraft = Minecraft.getInstance();
            if (evt.isUseItem() && minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.ENTITY) {

                Entity entity = ((EntityHitResult) minecraft.hitResult).getEntity();
                if (minecraft.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {

                    Vec3 hitVector = minecraft.hitResult.getLocation().subtract(entity.position());
                    EventResultHolder<InteractionResult> result = ArmorStandHandler.onUseEntityAt(minecraft.player, minecraft.level, evt.getHand(), entity, hitVector);
                    if (result.isInterrupt()) {
                        evt.setSwingHand(false);
                        evt.setCanceled(true);
                    }


                }
            }
        });
    }
}
