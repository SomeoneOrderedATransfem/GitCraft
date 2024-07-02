package transfem.order.gitcraft;

import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import transfem.order.gitcraft.Listeners.BlockListeners;
import transfem.order.gitcraft.Listeners.PlayerListeners;
import transfem.order.gitcraft.Listeners.TeleportListeners;
import transfem.order.gitcraft.util.Config;
import transfem.order.gitcraft.Commands.Commands;
import transfem.order.gitcraft.Commands.CommandsTabCompleter;

import java.io.File;
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;

public final class GitCraft extends JavaPlugin {

    @Override
    public void onEnable() {
        // Ensure data folder and subfolders are created
        createDirectory(getDataFolder());
        File commitFolder = new File(getDataFolder(), "commits");
        createDirectory(commitFolder);
        File inventoryFolder = new File(getDataFolder(), "inventory");
        createDirectory(inventoryFolder);
        File acceptedFolder = new File(getDataFolder(), "accepted");
        createDirectory(acceptedFolder);

        // Load worlds from commit folder
        loadCommitWorlds(commitFolder);


        // Apply Accepted Commits
        for (File file : acceptedFolder.listFiles()) {
            if (file.getName().endsWith(".mca")) {
                loadAcceptedCommit(file);
            }
        }

        // Load configuration
        Config.load();

        // Register event listeners
        registerListeners();

        // Register command executor and tab completer
        registerCommands();
    }

    private void createDirectory(File directory) {
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private void loadCommitWorlds(File commitFolder) {
        File[] files = commitFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                new WorldCreator(file.getName().replace(".yml", "")).createWorld();
            }
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new TeleportListeners(), this);
    }

    private void registerCommands() {
        getCommand("gitcraft").setExecutor(new Commands());
        getCommand("gitcraft").setTabCompleter(new CommandsTabCompleter());
    }

    private void loadAcceptedCommit(File file) {
        File sourceWorldPath = new File(getDataFolder(), "accepted");
        File destinationWorldPath = new File(getDataFolder(), Config.getWorld());
        String regionFileName = file.getName();

        File sourceFile = new File(sourceWorldPath, regionFileName);
        File destinationFile = new File(destinationWorldPath, regionFileName);

        try {
            copyFileUsingChannel(sourceFile, destinationFile);
            System.out.println("Region file copied successfully.");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public static GitCraft getInstance() {
        return JavaPlugin.getPlugin(GitCraft.class);
    }

    private static void copyFileUsingChannel(File source, File dest) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
