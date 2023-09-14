package fuzs.armorquickswap;

import com.google.common.collect.Lists;
import fuzs.armorquickswap.handler.ArmorStandGearHandler;
import fuzs.armorquickswap.mixin.accessor.OreConfigurationAccessor;
import fuzs.puzzleslib.api.biome.v1.BiomeLoadingPhase;
import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.BiomeModificationsContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.ServerLifecycleEvents;
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
    public void onRegisterBiomeModifications(BiomeModificationsContext context) {
        context.register(BiomeLoadingPhase.POST_PROCESSING, biomeLoadingContext -> {
            LOGGER.info("processing biome {}", biomeLoadingContext.getResourceKey().location());
            return true;
        }, context1 -> {

        });
    }

    @Override
    public ContentRegistrationFlags[] getContentRegistrationFlags() {
        return new ContentRegistrationFlags[]{ContentRegistrationFlags.BIOME_MODIFICATIONS};
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
