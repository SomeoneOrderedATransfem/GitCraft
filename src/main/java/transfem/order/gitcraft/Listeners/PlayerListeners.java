package transfem.order.gitcraft.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.yaml.snakeyaml.Yaml;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import org.bukkit.inventory.ItemStack;




public class PlayerListeners implements Listener {

    public static void playerReturn(Player player) {
        File inventoryFolder = new File(player.getWorld().getName(), "inventory");
        File playerInventory = new File(inventoryFolder, player.getUniqueId().toString() + ".yml");
        Yaml yaml = new Yaml();
        player.getInventory().clear();
        try {
            Map<String, Object> inventoryData = yaml.load(new FileInputStream(playerInventory));
            ItemStack[] inventoryContents = deserializeInventory(inventoryData);
            player.getInventory().setContents(inventoryContents);
            if (playerInventory.exists()) {
                playerInventory.delete();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading inventory for " + player.getName());
            System.out.println("Inventory file: " + playerInventory.getAbsolutePath());
            System.out.println("Contact someoneorderedatransfem with the error message above.");
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
    private static ItemStack[] deserializeInventory(Map<String, Object> inventoryData) {
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
