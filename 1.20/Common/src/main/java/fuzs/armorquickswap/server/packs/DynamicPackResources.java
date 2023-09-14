package fuzs.armorquickswap.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.resources.v1.AbstractModPackResources;
import net.minecraft.FileUtil;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicPackResources extends AbstractModPackResources {
    private static final Map<String, PackType> PATHS_FOR_TYPE = Stream.of(PackType.values()).collect(ImmutableMap.toImmutableMap(PackType::getDirectory, Function.identity()));

    private final Map<PackType, Map<ResourceLocation, IoSupplier<InputStream>>> paths;

    private DynamicPackResources(Collection<Function<PackOutput, DataProvider>> providers) {
        this.paths = generatePathsFromProviders(providers);
    }

    public static Supplier<AbstractModPackResources> create(Collection<Function<PackOutput, DataProvider>> providers) {
        return () -> new DynamicPackResources(providers);
    }

    private static Map<PackType, Map<ResourceLocation, IoSupplier<InputStream>>> generatePathsFromProviders(Collection<Function<PackOutput, DataProvider>> providers) {
        PackOutput packOutput = new PackOutput(Path.of(""));
        Map<PackType, Map<ResourceLocation, IoSupplier<InputStream>>> map = Stream.of(PackType.values()).collect(Collectors.toMap(Function.identity(), $ -> Maps.newTreeMap()));
        try {
            for (Function<PackOutput, DataProvider> provider : providers) {
                provider.apply(packOutput).run((Path filePath, byte[] data, HashCode hashCode) -> {
                    List<String> strings = FileUtil.decomposePath(filePath.normalize().toString()).get().left().filter(list -> list.size() >= 2).orElse(null);
                    if (strings != null) {
                        PackType packType = PATHS_FOR_TYPE.get(strings.get(0));
                        String path = strings.stream().skip(2).collect(Collectors.joining("/"));
                        ResourceLocation resourceLocation = ResourceLocation.tryBuild(strings.get(1), path);
                        if (resourceLocation != null) {
                            map.get(packType).put(resourceLocation, () -> new ByteArrayInputStream(data));
                        }
                    }
                }).get();
            }
        } catch (Throwable e) {
            ArmorQuickSwap.LOGGER.warn("Unable to construct dynamic pack resources", e);
            return Map.of();
        }
        map.replaceAll((packType, resourceLocationIoSupplierMap) -> {
            return ImmutableMap.copyOf(resourceLocationIoSupplierMap);
        });
        return Maps.immutableEnumMap(map);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        return this.paths.get(packType).get(location);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        this.paths.get(packType).entrySet().stream().filter(entry -> {
            return entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(path);
        }).forEach(entry -> {
            resourceOutput.accept(entry.getKey(), entry.getValue());
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return this.paths.get(type).keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
    }
}
