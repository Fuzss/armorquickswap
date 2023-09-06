package fuzs.armorquickswap;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
            # Configured Options
            
            This whole directory servers as a synchronized mirror of `.minecraft`. Every sub-directory and / or file placed within will be copied to the main `.minecraft` directory during game launch if the directory / file is not already present.
            There is no way of overriding an existing file, a copy will only be made when the target destination is empty.
            
            Examples:
            - `.minecraft/defaultpresets/options.txt` will be copied to `.minecraft/options.txt` if not already present
            - `.minecraft/defaultpresets/config/jei/jei.toml` will be copied to `.minecraft/config/jei/jei.toml` if not already present
            
            Note that this `README.md` file is excluded from being copied to `.minecraft`.
            """;

    private static boolean initialized;

    public YourOptionsShallBeRespected() {

        // this is constructed twice, we only need to run once
        // (once when Forge is counting available language loaders, and the second time when the language loader is actually used)
        if (initialized) return;

        initialized = true;
        LOGGER.info("Applying default files...");

        try {
            trySetupFresh();
            tryCopyFiles();
        } catch (IOException e) {
            LOGGER.error("Failed to setup default files", e);
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
