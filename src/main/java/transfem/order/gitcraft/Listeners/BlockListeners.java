package transfem.order.gitcraft.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import transfem.order.gitcraft.GitCraft;

public class BlockListeners implements Listener {

    private static final Logger logger = GitCraft.getInstance().getLogger();

    private void logRegion(BlockEvent event) {
        String worldName = event.getBlock().getWorld().getName();
        if (worldName.startsWith("git")) {
            File yamlFile = new File(GitCraft.getInstance().getDataFolder(), worldName + ".yml");
            if (yamlFile.exists()) {
                try (FileReader reader = new FileReader(yamlFile)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(reader);

                    int blockY = event.getBlock().getY();
                    int minY = Integer.parseInt(data.get("minY").toString());
                    int maxY = Integer.parseInt(data.get("maxY").toString());

                    if (blockY < minY) data.put("minY", blockY);
                    if (blockY > maxY) data.put("maxY", blockY);

                    List<String> regions = (List<String>) data.get("regions");
                    int regionX = event.getBlock().getX() >> 9;
                    int regionZ = event.getBlock().getZ() >> 9;
                    String regionKey = regionX + "," + regionZ;

                    if (!regions.contains(regionKey)) {
                        regions.add(regionKey);
                        data.put("regions", regions);

                        try (FileWriter writer = new FileWriter(yamlFile)) {
                            yaml.dump(data, writer);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Error writing to YAML file: " + yamlFile.getName(), e);
                        }
                    }

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error reading YAML file: " + yamlFile.getName(), e);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        logRegion(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        logRegion(event);
    }
}
