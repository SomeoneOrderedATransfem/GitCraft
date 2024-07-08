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
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import transfem.order.gitcraft.GitCraft;

public class PlayerListeners implements Listener {

    private static final Logger logger = GitCraft.getInstance().getLogger();
    private static final Yaml yaml = new Yaml();

    /**
     * Restores the player's inventory from a saved file.
     *
     * @param player The player whose inventory should be restored.
     */
    public static void playerReturn(Player player) {
        File inventoryFolder = new File(GitCraft.getInstance().getDataFolder(), "inventory");
        if (!inventoryFolder.exists()) {
            inventoryFolder.mkdirs();
        }

        File playerInventory = new File(inventoryFolder, player.getUniqueId().toString() + ".yml");
        player.getInventory().clear();

        if (playerInventory.exists()) {
            try (FileInputStream fis = new FileInputStream(playerInventory)) {
                Map<String, Object> inventoryData = yaml.load(fis);
                ItemStack[] inventoryContents = deserializeInventory(inventoryData);
                player.getInventory().setContents(inventoryContents);
                if (!playerInventory.delete()) {
                    logger.log(Level.WARNING, "Failed to delete inventory file for player: " + player.getName());
                }
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE, "Inventory file not found for player: " + player.getName(), e);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading inventory file for player: " + player.getName(), e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading inventory for player: " + player.getName(), e);
                logger.log(Level.SEVERE, "Inventory file: " + playerInventory.getAbsolutePath());
                logger.log(Level.SEVERE, "Contact someoneorderedatransfem with the error message above.");
            }
        }
    }

    /**
     * Handles the player quit event to save and clear their inventory if they are in a commit world.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().startsWith("commit")) {
            playerReturn(player);
        }
    }

    /**
     * Deserializes the player's inventory from a map.
     *
     * @param inventoryData The serialized inventory data.
     * @return The deserialized inventory contents.
     */
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
