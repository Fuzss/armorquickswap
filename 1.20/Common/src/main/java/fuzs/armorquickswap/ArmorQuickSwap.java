package fuzs.armorquickswap;

import com.google.common.collect.Lists;
import fuzs.armorquickswap.handler.ArmorStandGearHandler;
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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collection;
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

            Unsafe unsafe;
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            Registry<Feature<?>> featureRegistry = server.registryAccess().registryOrThrow(Registries.FEATURE);
            Registry<ConfiguredFeature<?, ?>> configuredFeatureRegistry = server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
            for (ConfiguredFeature<?, ?> feature : configuredFeatureRegistry) {
                if (feature.config() instanceof OreConfiguration || featureRegistry.getKey(feature.feature()).equals(new ResourceLocation("ore")) || featureRegistry.getKey(feature.feature()).equals(new ResourceLocation("scattered_ore"))) {

                    Field field1 = null;
                    for (Field field : feature.config().getClass().getDeclaredFields()) {
                        if (field.getGenericType() == OreConfiguration.TargetBlockState.class && Collection.class.isAssignableFrom(field.getType())) {
                            field1 = field;
                            break;
                        }
                    }

                    if (field1 != null) {

                        field1.setAccessible(true);
                        try {
                            Collection<OreConfiguration.TargetBlockState> targets = (Collection<OreConfiguration.TargetBlockState>) MethodHandles.lookup().unreflectGetter(field1).invoke(feature.config());

                            List<OreConfiguration.TargetBlockState> targets2 = Lists.newArrayList(targets);

                            for (int i = 0; i < targets2.size(); i++) {
                                OreConfiguration.TargetBlockState state = targets2.get(i);
                                if (state.state.is(BlockTags.COAL_ORES)) {
                                    targets2.add(i++, OreConfiguration.target(state.target, Blocks.COAL_BLOCK.defaultBlockState()));
                                }
                            }

                            if (targets.size() != targets2.size()) {
                                long fieldOffset = unsafe.objectFieldOffset(field1);
                                unsafe.putObject(targets2, fieldOffset, feature.config());
                            }

                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
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
