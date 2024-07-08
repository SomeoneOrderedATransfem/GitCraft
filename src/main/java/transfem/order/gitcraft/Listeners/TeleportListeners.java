package transfem.order.gitcraft.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import transfem.order.gitcraft.GitCraft;

public class TeleportListeners implements Listener {

    private static final Logger logger = GitCraft.getInstance().getLogger();
    private static final Yaml yaml = new Yaml();

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().getName().startsWith("commit")) {
            File inventoryFolder = new File(GitCraft.getInstance().getDataFolder(), "inventory");
            File playerInventory = new File(inventoryFolder, event.getPlayer().getUniqueId().toString() + ".yml");

            if (playerInventory.exists()) {
                try (FileReader reader = new FileReader(playerInventory)) {
                    Map<String, Object> inventoryData = yaml.load(reader);
                    event.getPlayer().getInventory().setContents(PlayerListeners.deserializeInventory(inventoryData));
                    if (!playerInventory.delete()) {
                        logger.log(Level.WARNING, "Failed to delete inventory file for player: " + event.getPlayer().getName());
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error loading inventory for player: " + event.getPlayer().getName(), e);
                    event.getPlayer().getInventory().clear();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.getRespawnLocation().getWorld().getName().startsWith("commit")) {
            File inventoryFolder = new File(GitCraft.getInstance().getDataFolder(), "inventory");
            File playerInventory = new File(inventoryFolder, event.getPlayer().getUniqueId().toString() + ".yml");

            if (playerInventory.exists()) {
                try (FileReader reader = new FileReader(playerInventory)) {
                    Map<String, Object> inventoryData = yaml.load(reader);
                    event.getPlayer().getInventory().setContents(PlayerListeners.deserializeInventory(inventoryData));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error loading inventory for player: " + event.getPlayer().getName(), e);
                    event.getPlayer().getInventory().clear();
                }
            }
        }
    }
}
