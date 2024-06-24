package transfem.order.gitcraft.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.yaml.snakeyaml.Yaml;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import transfem.order.gitcraft.GitCraft;

public class PlayerListeners implements Listener {

    private static final Logger logger = GitCraft.getInstance().getLogger();

    public static void playerReturn(Player player) {
        File inventoryFolder = new File(GitCraft.getInstance().getDataFolder(), "inventory");
        if (!inventoryFolder.exists()) {
            inventoryFolder.mkdirs();
        }

        File playerInventory = new File(inventoryFolder, player.getUniqueId().toString() + ".yml");
        Yaml yaml = new Yaml();
        player.getInventory().clear();
        try {
            if (playerInventory.exists()) {
                Map<String, Object> inventoryData = yaml.load(new FileInputStream(playerInventory));
                ItemStack[] inventoryContents = deserializeInventory(inventoryData);
                player.getInventory().setContents(inventoryContents);
                if (playerInventory.exists()) {
                    playerInventory.delete();
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Inventory file not found for player: " + player.getName(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading inventory for player: " + player.getName(), e);
            logger.log(Level.SEVERE, "Inventory file: " + playerInventory.getAbsolutePath());
            logger.log(Level.SEVERE, "Contact someoneorderedatransfem with the error message above.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().startsWith("commit")) {
            playerReturn(player);
        }
    }

    @SuppressWarnings("unchecked")
    static ItemStack[] deserializeInventory(Map<String, Object> inventoryData) {
        ItemStack[] inventoryContents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            Map<String, Object> itemData = (Map<String, Object>) inventoryData.get(String.valueOf(i));
            if (itemData != null) {
                inventoryContents[i] = ItemStack.deserialize(itemData);
            }
        }
        return inventoryContents;
    }
}
