package fuzs.armorquickswap;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.world.level.block.Blocks;

public class ArmorQuickSwapFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
        FlattenableBlockRegistry.register(Blocks.DIRT_PATH, Blocks.DIRT.defaultBlockState());
        StrippableBlockRegistry.register(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LOG);
    }
}
