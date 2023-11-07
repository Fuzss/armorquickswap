package fuzs.armorquickswap;

import com.google.common.collect.Lists;
import fuzs.armorquickswap.handler.ArmorStandGearHandler;
import fuzs.armorquickswap.mixin.accessor.OreConfigurationAccessor;
import fuzs.armorquickswap.server.packs.DeepslateRecipeProvider;
import fuzs.armorquickswap.server.packs.SawmillRecipeProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.BlockInteractionsContext;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.ServerLifecycleEvents;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ArmorQuickSwap implements ModConstructor {
    public static final String MOD_ID = "armorquickswap";
    public static final String MOD_NAME = "Armor Quick Swap";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        // run before other mods like Quark that might interfere here
        PlayerInteractEvents.USE_ENTITY_AT.register(EventPhase.BEFORE, ArmorStandGearHandler::onUseEntityAt);

        ServerLifecycleEvents.STARTING.register(server -> {

            Registry<ConfiguredFeature<?, ?>> registry = server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
            for (ConfiguredFeature<?, ?> feature : registry) {
                if (feature.config() instanceof OreConfiguration oreConfiguration) {

                    List<OreConfiguration.TargetBlockState> targetStates = Lists.newArrayList(oreConfiguration.targetStates);

                    for (int i = 0; i < targetStates.size(); i++) {
                        OreConfiguration.TargetBlockState state = targetStates.get(i);
                        if (state.state.is(BlockTags.COAL_ORES)) {
                            RandomForwardingRuleTest target = new RandomForwardingRuleTest(state.target, 0.5F);
                            targetStates.add(i++, OreConfiguration.target(target, Blocks.COAL_BLOCK.defaultBlockState()));
                        }
                    }

                    if (oreConfiguration.targetStates.size() != targetStates.size()) {
                        ((OreConfigurationAccessor) oreConfiguration).armorquickswap$setTargetStates(targetStates);
                    }
                }
            }
        });
    }

    @Override
    public void onRegisterBlockInteractions(BlockInteractionsContext context) {
        context.registerStrippable(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG);
        context.registerStrippable(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD);
        context.registerFlattenable(Blocks.DIRT, Blocks.DIRT_PATH);
        context.registerFlattenable(Blocks.DIRT_PATH, Blocks.FARMLAND);
        context.registerTillable(Blocks.FARMLAND, Blocks.DIRT_PATH);
        context.registerTillable(Blocks.DIRT, Blocks.FARMLAND);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        context.addRepositorySource(PackResourcesHelper.buildServerPack(id("dynamic_recipes"), DynamicPackResources.create(SawmillRecipeProvider::new, DeepslateRecipeProvider::new), false));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
