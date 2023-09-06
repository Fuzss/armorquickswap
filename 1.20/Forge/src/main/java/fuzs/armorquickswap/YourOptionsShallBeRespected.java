package fuzs.armorquickswap;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.ModSorter;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModValidator;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class YourOptionsShallBeRespected implements IModLanguageProvider {
    public static final Logger LOGGER = LogManager.getLogger("YOSBR");
    public static final Path GAME_PATH = FMLPaths.GAMEDIR.get();
    public static final Path GAME_PARENT_PATH = FMLPaths.GAMEDIR.get().getParent();
    public static final File RUN_DIR = GAME_PATH.toFile();
    public static final File CONFIG_DIR = FMLPaths.CONFIGDIR.get().toFile();
    public static final String DEFAULT_PRESETS_DIRECTORY = "defaultpresets";
    public static final Path DEFAULT_PRESETS_PATH = GAME_PATH.resolve(DEFAULT_PRESETS_DIRECTORY);
    public static final String README_FILE = "README.md";
    public static final Path README_PATH = DEFAULT_PRESETS_PATH.resolve(README_FILE);
    private static final Set<Path> BLACKLISTED_PATHS = Set.of(DEFAULT_PRESETS_PATH, README_PATH);
    public static final String README_CONTENT = """
            # Your Options Shall Be Respected
            
            
            
            Note that this file is excluded from being copied to `.minecraft`.
            """;

    private static boolean initialized;

    private static String fileToLine(ModList modList, IModFile mf) {
        return String.format(Locale.ENGLISH, "%-50.50s|%-30.30s|%-30.30s|%-20.20s|%-10.10s|Manifest: %s", mf.getFileName(),
                mf.getModInfos().get(0).getDisplayName(),
                mf.getModInfos().get(0).getModId(),
                mf.getModInfos().get(0).getVersion(),
                getModContainerState(modList, mf.getModInfos().get(0).getModId()),
                ((ModFileInfo)mf.getModFileInfo()).getCodeSigningFingerprint().orElse("NOSIGNATURE"));
    }

    private static String getModContainerState(ModList modList, String modId) {
        if (true) return "NONE";
        return modList.getModContainerById(modId).map(ModContainer::getCurrentState).map(Object::toString).orElse("NONE");
    }

    private static String crashReport(ModList modList) {
        return "\n"+modList.applyForEachModFile(iModFile -> fileToLine(modList, iModFile)).collect(Collectors.joining("\n\t\t", "\t\t", ""));
    }

    /**
     * We are using pre-launch entrypoint here as we want to be faster than everyone.
     */
    public YourOptionsShallBeRespected() {

        // this is constructed twice for some reason, we only need to run once
        if (initialized) return;

        initialized = true;

        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Field modValidatorField = FMLLoader.class.getDeclaredField("modValidator");
            modValidatorField.setAccessible(true);
            ModValidator modValidator = (ModValidator) lookup.unreflectGetter(modValidatorField).invoke();
            Field candidateModsField = ModValidator.class.getDeclaredField("candidateMods");
            candidateModsField.setAccessible(true);
            List<ModFile> candidateMods = (List<ModFile>) lookup.unreflectGetter(candidateModsField).invoke(modValidator);
            LoadingModList loadingModList = ModSorter.sort(candidateMods, List.of());
            loadingModList.addCoreMods();
            loadingModList.addAccessTransformers();
            ModList modList = ModList.of(loadingModList.getModFiles().stream().map(ModFileInfo::getFile).toList(), loadingModList.getMods());
            LOGGER.info("Loading {} mods:" + crashReport(modList), loadingModList.getModFiles().size());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }


        LOGGER.info("Applying default files...");

        try {
            trySetupFresh();
            tryCopyFiles();
        } catch (IOException e) {
            LOGGER.error("Failed to setup default files", e);
        }



        if (true) return;

        try {
            // creates yosbr directory
            File yosbr = new File(CONFIG_DIR, "yosbr");
            if (!yosbr.exists() && !yosbr.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + yosbr.getAbsolutePath());
            }
            // creates an empty options file
            new File(yosbr, "options.txt").createNewFile();
            // creates an empty config file
            File config = new File(yosbr, "config");
            if (!config.exists() && !config.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + config.getAbsolutePath());
            }
            Files.walk(yosbr.toPath()).forEach(path -> {
                File file = path.normalize().toAbsolutePath().normalize().toFile();
                if (!file.isFile()) return;
                try {
                    try {
                        Path configRelative = config.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize());
                        if (configRelative.startsWith("yosbr"))
                            throw new IllegalStateException("Illegal default config file: " + file);
                        this.applyDefaultOptions(new File(CONFIG_DIR, configRelative.normalize().toString()), file);
                    } catch (IllegalArgumentException e) {
                        System.out.println(yosbr.toPath().toAbsolutePath().normalize());
                        System.out.println(file.toPath().toAbsolutePath().normalize());
                        System.out.println(yosbr.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()));
                        this.applyDefaultOptions(new File(RUN_DIR, yosbr.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()).normalize().toString()), file);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to apply default options.", e);
        }
    }

    private static void trySetupFresh() throws IOException {
        File sourceFile = DEFAULT_PRESETS_PATH.toFile();
        if (!sourceFile.exists()) {
            if (!sourceFile.mkdir()) {
                LOGGER.info("Failed to create fresh {} directory", DEFAULT_PRESETS_PATH.relativize(GAME_PARENT_PATH));
            } else {
                LOGGER.info("Successfully created fresh {} directory", DEFAULT_PRESETS_PATH.relativize(GAME_PARENT_PATH));
                Files.write(README_PATH, README_CONTENT.getBytes());
                LOGGER.info("Successfully created fresh {} file", README_PATH.relativize(GAME_PARENT_PATH));
            }
        }
    }

    private static void tryCopyFiles() throws IOException {
        Files.walk(DEFAULT_PRESETS_PATH).forEach(sourcePath -> {
            if (BLACKLISTED_PATHS.contains(sourcePath)) return;
            Path targetPath = GAME_PATH.resolve(DEFAULT_PRESETS_PATH.relativize(sourcePath)).normalize();
            // check if file already exists, otherwise copy will throw an exception
            if (sourcePath.toFile().exists() && !targetPath.toFile().exists()) {
                try {
                    // we do not need to handle creating parent directories as the file tree is traversed depth-first
                    Files.copy(sourcePath, targetPath);
                    LOGGER.info("Successfully copied {} to {}", sourcePath.relativize(GAME_PARENT_PATH), targetPath.relativize(GAME_PARENT_PATH));
                } catch (IOException e) {
                    LOGGER.info("Failed to copy {} to {}", sourcePath.relativize(GAME_PARENT_PATH), targetPath.relativize(GAME_PARENT_PATH));
                }
            }
        });
    }

    private void applyDefaultOptions(File file, File defaultFile) throws IOException {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + file.getParentFile().getAbsolutePath());
        }
        if (!defaultFile.getParentFile().exists() && !defaultFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + defaultFile.getParentFile().getAbsolutePath());
        }
        if (!defaultFile.exists()) {
            defaultFile.createNewFile();
            return;
        }
        if (file.exists()) return;
        LOGGER.info("Applying default options for " + File.separator + RUN_DIR.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()).normalize() + " from " + File.separator + RUN_DIR.toPath().toAbsolutePath().normalize().relativize(defaultFile.toPath().toAbsolutePath().normalize()).normalize());
        Files.copy(defaultFile.toPath(), file.toPath());
    }
    @Override
    public String name() {
        return "YOSBR";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return modFileScanData -> {};
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {

    }
}
