package transfem.order.gitcraft.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().getName().startsWith("commit")) {
            File inventoryFolder = new File(GitCraft.getInstance().getDataFolder(), "inventory");
            File playerInventory = new File(inventoryFolder, event.getPlayer().getUniqueId().toString() + ".yml");
            Yaml yaml = new Yaml();

            if (playerInventory.exists()) {
                try {
                    Map<String, Object> inventoryData = yaml.load(new FileReader(playerInventory));
                    event.getPlayer().getInventory().setContents(PlayerListeners.deserializeInventory(inventoryData));
                    playerInventory.delete();
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
                Yaml yaml = new Yaml();
                try {
                    Map<String, Object> inventoryData = yaml.load(new FileReader(playerInventory));
                    event.getPlayer().getInventory().setContents(PlayerListeners.deserializeInventory(inventoryData));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error loading inventory for player: " + event.getPlayer().getName(), e);
                    event.getPlayer().getInventory().clear();
                }
            }
        }
    }
}
