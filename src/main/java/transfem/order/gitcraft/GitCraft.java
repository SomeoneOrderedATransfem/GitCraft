package transfem.order.gitcraft;

import org.bukkit.plugin.java.JavaPlugin;

import transfem.order.gitcraft.Listeners.Listeners;
import transfem.order.gitcraft.util.Config;
import transfem.order.gitcraft.Commands.Commands;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;


public final class GitCraft extends JavaPlugin {
    @Override
    public void onEnable() {
        // Check for Data Folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        // Load Config
        Config.load();

        //Register Listeners
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        // set command excecutor
        this.getCommand("gitcraft").setExecutor(new Commands());
        
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
