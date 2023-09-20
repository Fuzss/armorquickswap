package fuzs.armorquickswap;

import fuzs.armorquickswap.data.ModBlockTagProvider;
import fuzs.armorquickswap.data.ModEntityTypeTagProvider;
import fuzs.armorquickswap.data.ModParticleDescriptionProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(ArmorQuickSwap.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ArmorQuickSwapForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        if (ModLoaderEnvironment.INSTANCE.isServer()) return;
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener((final BlockEvent.BlockToolModificationEvent evt) -> {
            if (evt.getToolAction() == ToolActions.SHOVEL_FLATTEN) {
                if (evt.getState().is(Blocks.DIRT_PATH)) {
                    evt.setFinalState(Blocks.DIRT.defaultBlockState());
                }
            } else if (evt.getToolAction() == ToolActions.AXE_STRIP) {
                if (evt.getState().is(Blocks.STRIPPED_OAK_LOG)) {
                    evt.setFinalState(Blocks.OAK_LOG.defaultBlockState());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent evt) {
        evt.getGenerator().addProvider(true, new ModBlockTagProvider(evt, ArmorQuickSwap.MOD_ID));
        evt.getGenerator().addProvider(true, new ModEntityTypeTagProvider(evt, ArmorQuickSwap.MOD_ID));
        evt.getGenerator().addProvider(true, new ModParticleDescriptionProvider(evt, ArmorQuickSwap.MOD_ID));
    }
}
