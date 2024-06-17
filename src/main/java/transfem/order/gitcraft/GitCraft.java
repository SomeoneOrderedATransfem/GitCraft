package transfem.order.gitcraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;

import transfem.order.gitcraft.Listeners.Listeners;
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
        // Load Config
        Config.load();

        //Register Listeners
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        // set command excecutor
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check for if player is in
    }

}
