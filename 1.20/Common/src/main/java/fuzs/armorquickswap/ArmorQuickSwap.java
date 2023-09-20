package fuzs.armorquickswap;

import com.google.common.collect.Lists;
import fuzs.armorquickswap.handler.ArmorStandGearHandler;
import fuzs.armorquickswap.handler.RightClickHarvestHandler;
import fuzs.armorquickswap.init.ModRegistry;
import fuzs.armorquickswap.mixin.accessor.OreConfigurationAccessor;
import fuzs.armorquickswap.server.packs.DynamicPackResources;
import fuzs.armorquickswap.server.packs.ModRecipeProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.entity.ServerEntityLevelEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import fuzs.puzzleslib.api.event.v1.server.ServerLifecycleEvents;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
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
        ModRegistry.touch();
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
        PlayerInteractEvents.USE_BLOCK.register(RightClickHarvestHandler::onUseBlock);
        BlockEvents.FARMLAND_TRAMPLE.register((level, pos, state, fallDistance, entity) -> {
            return EventResult.INTERRUPT;
        });
        ServerEntityLevelEvents.SPAWN.register((entity, level, spawnType) -> {
            // don't affect when profession is forced during summoning via command, which is achieved by the null check
            // MobSpawnType#COMMAND on the other hand only targets mobs without predefined nbt tag which should not allow for nitwits
            // also don't affect mob conversions (e.g. zombie kills villager), also achieved by null check (and not MobSpawnType#CONVERSION, as it is set too late)
            if (spawnType != null) {
                if (entity instanceof VillagerDataHolder holder && holder.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
                    holder.setVillagerData(holder.getVillagerData().setProfession(VillagerProfession.NONE));
                }
            }
            return EventResult.PASS;
        });
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        context.addRepositorySource(PackResourcesHelper.buildServerPack(DynamicPackResources.create(List.of(ModRecipeProvider::new)), "woodcutting_recipes", Component.literal(MOD_NAME), CommonComponents.EMPTY, true, false));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
