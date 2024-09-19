package fuzs.armorquickswap;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class ArmorQuickSwapFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
        LandPathNodeTypesRegistry.register(Blocks.STONECUTTER, BlockPathTypes.DAMAGE_OTHER, BlockPathTypes.DANGER_OTHER);
    }
}
