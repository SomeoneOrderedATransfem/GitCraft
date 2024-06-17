package transfem.order.gitcraft.util;

import org.bukkit.configuration.file.YamlConfiguration;
import transfem.order.gitcraft.GitCraft;
import java.io.File;

public class Config {
    private final static Config instance = new Config();

    private static File configFile;
    private static YamlConfiguration config;

    private static String worldName;
    private static int chunkSize;

    private Config() {
    }

    public static void load() {
        configFile = new File(GitCraft.getInstance().getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            GitCraft.getInstance().saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        config.options().parseComments(true);

        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        worldName = config.getString("worldName");
        chunkSize = config.getInt("chunkSize");
    }

    public static void save() {
        config.set("worldName", worldName);
        config.set("chunkSize", chunkSize);

        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void set(String path, Object value) {
        config.set(path, value);

        save();
    }

    public static String WorldName() {
        return worldName;
    }

    public static int ChunkRadius() {
        return chunkSize;
    }

    public static void setWorldName(String worldName) {
        Config.worldName = worldName;

        set("worldName", worldName);
    }

    public static void setChunkRadius(int chunkSize) {
        Config.chunkSize = chunkSize;

        set("chunkSize", chunkSize);
    }

    public static Config getInstance() {
        return instance;
    }

}
