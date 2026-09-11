package CCPCT.better_stacking.modConfig;

import CCPCT.better_stacking.BetterStacking;
import CCPCT.better_stacking.util.EntityClusterManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ModConfig {

    public boolean modEnabled = true;
    public int entityUpdateTimeInterval = 20;
    public boolean renderThroughBlocks = false;
    public int labelColour = 0xA0FFFF00;
    public int labelBgColour = 0x67676767;
    public float labelSize = 1f;
    public float labelOffset = 10f;

    public boolean itemGeneral = false;
    public int itemCount = 1;
    public boolean itemShowLabel = true;
    public boolean itemLabelShowName = true;
    public int itemSuffixMode = 0;

    public boolean entityGeneral = false;
    public int entityCount = 5;
    public boolean entityShowLabel = true;
    public boolean entityLabelShowName = true;
    public int entitySuffixMode = 0;

    public boolean xpGeneral = false;
    public int xpCount = 1;
    public boolean xpShowLabel = true;
    public int xpSuffixMode = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve(BetterStacking.MOD_ID + ".json");

    private static ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static synchronized void load() {
        ModConfig loaded = null;

        if (Files.exists(CONFIG_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                loaded = GSON.fromJson(reader, ModConfig.class);
            } catch (IOException | RuntimeException e) {
                // Unreadable or malformed file: fall back to defaults rather than crash the client.
                BetterStacking.LOGGER.error("Could not read {}, using defaults", CONFIG_PATH, e);
            }
        }

        INSTANCE = loaded != null ? loaded : new ModConfig();
        INSTANCE.clamp();

        if (loaded == null) save();
        EntityClusterManager.invalidate();
    }

    public static synchronized void save() {
        get().clamp();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            // Write beside the real file first, so a crash mid write cannot truncate the config.
            Path temp = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(get()));
            try {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            BetterStacking.LOGGER.error("Unable to save the Better Stacking config", e);
        }
        EntityClusterManager.invalidate();
    }

    /** Keeps hand edited files from producing division by zero, invisible labels or endless scans. */
    private void clamp() {
        entityUpdateTimeInterval = Math.clamp(entityUpdateTimeInterval, 1, 1200);
        labelSize = Math.clamp(labelSize, 0.1f, 10f);
        labelOffset = Math.clamp(labelOffset, -64f, 64f);

        itemCount = Math.clamp(itemCount, 1, 1000);
        entityCount = Math.clamp(entityCount, 1, 1000);
        xpCount = Math.clamp(xpCount, 1, 1000);

        itemSuffixMode = Math.clamp(itemSuffixMode, 0, 2);
        entitySuffixMode = Math.clamp(entitySuffixMode, 0, 2);
        xpSuffixMode = Math.clamp(xpSuffixMode, 0, 2);
    }
}
