package transfem.order.gitcraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;

import transfem.order.gitcraft.Listeners.BlockListeners;
import transfem.order.gitcraft.Listeners.PlayerListeners;
import transfem.order.gitcraft.util.Config;
import transfem.order.gitcraft.Commands.Commands;
import transfem.order.gitcraft.Commands.CommandsTabCompleter;

import java.io.File;




public final class GitCraft extends JavaPlugin {
    @Override
    public void onEnable() {
        // Check for Data Folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        // Check if the folder "commit" exists
        File commitFolder = new File(getDataFolder(), "commits");
        if (!commitFolder.exists()) {
            commitFolder.mkdir();
        }
        File inventoryFolder = new File(getDataFolder(), "inventory");
        if (!inventoryFolder.exists()) {
            inventoryFolder.mkdir();
        }
        // Load Config
        Config.load();

        //Register Listeners
        getServer().getPluginManager().registerEvents(new BlockListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);

        this.getCommand("gitcraft").setExecutor(new Commands());

        // set tab completer
        this.getCommand("gitcraft").setTabCompleter(new CommandsTabCompleter());

        
    }

    public static GitCraft getInstance() {
        return JavaPlugin.getPlugin(GitCraft.class);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
