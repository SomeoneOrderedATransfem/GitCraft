package transfem.order.gitcraft.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;

import transfem.order.gitcraft.GitCraft;

import java.io.File;

public class Config {
    private final static Config instance = new Config();

    private static File configFile;
    private static YamlConfiguration config;

    private static String worldName;
    private static int chunkSize;
    private static Location returnLocation;

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
        returnLocation = new Location(GitCraft.getInstance().getServer().getWorld(worldName), config.getDouble("returnLocation.x"), config.getDouble("returnLocation.y"), config.getDouble("returnLocation.z"));
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

    public static String getWorld() {
        return worldName;
    }

    public static int getRadius() {
        return chunkSize;
    }

    public static Location getReturnLocation() {
        return returnLocation;
    }


    public static void setReturnLocation(Location location) {
        returnLocation = location;
    }

    public static void setWorld(String worldName) {
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
