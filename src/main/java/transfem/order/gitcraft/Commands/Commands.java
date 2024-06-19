package transfem.order.gitcraft.Commands;

import transfem.order.gitcraft.GitCraft;
import transfem.order.gitcraft.util.Config;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.util.Random;

// import snake yaml
import org.yaml.snakeyaml.Yaml;







public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("gitcraft")) return false;
        // Help Command
        if (args.length == 0 || args[0] == "help") {
            if (sender.hasPermission("gitcraft.command"))
            sender.sendMessage("GitCraft Commands:",
                "/gitcraft - GitCraft Command",
                "/gitcraft help - View all avaliable commands",
                "/gitcraft view - View all Commits",
                "/gitcraft view <commit> - View a specific commit");
            if (sender.hasPermission("gitcraft.command.commit"))
            sender.sendMessage("/gitcraft checkout - Checkout the current world to a new commit",
                "/gitcraft commit - View the commit subcommands",
                "/gitcraft commit view - View the current data of the commit",
                "/gitcraft commit set-message <message> - Set the commit message",
                "/gitcraft commit contributor add <player> - Add a contributor to the commit",
                "/gitcraft commit contributor remove <player> - Remove a contributor from the commit",
                "/gitcraft commit contributor list - List all contributors of the commit",
                "/gitcraft commit cancel-commit - Cancel the current commit");
            if (sender.hasPermission("gitcraft.commit.manage"))
            sender.sendMessage("/gitcraft accept-commit <world> - Accept the commit of the specified world",
                "/gitcraft reject-commit <world> <reason> - Reject the commit of the specified world");
            if (sender.hasPermission("gitcraft.command.manage"))
            sender.sendMessage("/gitcraft config - View the configuration of GitCraft",
                "/gitcraft config set-world <world> - Set the world to use for GitCraft",
                "/gitcraft config set-world - Set the world to use for GitCraft to this world",
                "/gitcraft config set-chunk-radius <radius> - Set the chunk radius for GitCraft",
                "/gitcraft config view - View the current configuration of GitCraft");
            return true;
        }
        // View Command
        if (args[0].equalsIgnoreCase("view") && sender.hasPermission("gitcraft.command")) {
            if (args.length == 1) {
                File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
                File[] commits = commitFolder.listFiles();
                if (commits == null) {
                    sender.sendMessage("No commits found");
                    return true;
                }
                for (File commit : commits) {
                    sender.sendMessage(commit.getName());
                }
                return true;
            }
            if (args.length == 2) {
                File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
                File commit = new File(commitFolder, args[1]);
                if (!commit.exists()) {
                    sender.sendMessage("Commit not found");
                    return true;
                }
                Player player = (Player) sender;
                player.teleport(new Location(sender.getServer().getWorld(args[1]), player.getX(), player.getY(), player.getZ()));
                return true;
            }
        }
        // Config Commands
        if (args[0].equalsIgnoreCase("config") && sender.hasPermission("gitcraft.manage")) {
            if (args.length == 1) {
                sender.sendMessage("GitCraft Configuration:",
                    "/gitcraft config set-world <world> - Set the world to use for GitCraft",
                    "/gitcraft config set-world - Set the world to use for GitCraft to this world",
                    "/gitcraft config set-chunk-radius <radius> - Set the chunk radius for GitCraft",
                    "/gitcraft config view - View the current configuration of GitCraft");
                return true;
            }
            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("view")) {
                    sender.sendMessage("GitCraft Configuration:",
                        "World: " + Config.WorldName(),
                        "Chunk Radius: " + Config.ChunkRadius());
                    return true;
                }
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("set-world")) {
                    Config.setWorldName(args[2]);
                    sender.sendMessage("World set to " + args[2]);
                    return true;
                }
                if (args[1].equalsIgnoreCase("set-chunk-radius")) {
                    Config.setChunkRadius(Integer.parseInt(args[2]));
                    sender.sendMessage("Chunk Radius set to " + args[2]);
                    return true;
                }
            }
        }
        if (args[0].equalsIgnoreCase("checkout") && sender.hasPermission("gitcraft.commit")) {
            sender.sendMessage("Checking out world to new commit");
            File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
            File commit = new File(commitFolder, "commit-" + System.currentTimeMillis() + ".yml");
            try {
                String hexString = Integer.toHexString(new Random().nextInt());
                commit.createNewFile();
                Yaml yaml = new Yaml();
                Map<String, Object> data = new HashMap<>();
                data.put("message", "No message");
                data.put("contributors", new ArrayList<String>());
                yaml.dump(data, new FileWriter(commit));
                sender.sendMessage("Commit created");
                sender.getServer().createWorld(new WorldCreator("commit-" + hexString).type(WorldType.FLAT).generatorSettings("minecraft:air;minecraft;plains;"));
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask(GitCraft.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Player player = (Player) sender;
                        for (int x = -Config.ChunkRadius()*16; x < Config.ChunkRadius()*16; x++) {
                            for (int z = -Config.ChunkRadius(*16; z < Config.ChunkRadius()*16; z++) {
                                for (int y = -62; y < 256; y++) {
                                    Chunk chunk = player.getWorld().getChunkAt(x, z);
                                    player.getWorld().loadChunk(chunk);
                                    player.getWorld().getBlockAt(x, y, z).setType(player.getWorld().getBlockAt(x, y, z).getType());
                                }
                            }
                        }

                    }
                }, 20L);
                return true;
            } catch (IOException e) {
                sender.sendMessage("Error creating commit",
                "IO Exception, please report to an admin or someoneorderedatransfem");
                sender.sendMessage(e.toString());
                e.printStackTrace();
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("commit") && sender.hasPermission("gitcraft.commit")) {
            if (args.length == 1) {
                sender.sendMessage("GitCraft Commit Commands:",
                    "/gitcraft commit view - View the current data of the commit",
                    "/gitcraft commit set-message <message> - Set the commit message",
                    "/gitcraft commit contributor add <player> - Add a contributor to the commit",
                    "/gitcraft commit contributor remove <player> - Remove a contributor from the commit",
                    "/gitcraft commit contributor list - List all contributors of the commit",
                    "/gitcraft commit cancel-commit - Cancel the current commit");
                return true;
            }
            if (args[1].equalsIgnoreCase("view")) {
                File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
                File commit = new File(commitFolder, "commit-" + System.currentTimeMillis() + ".yml");
                if (!commit.exists()) {
                    sender.sendMessage("No commit found");
                    return true;
                }
                Yaml yaml = new Yaml();
                try (FileReader fileReader = new FileReader(commit)) {
                    Map<String, Object> data = yaml.load(fileReader);
                    sender.sendMessage("Commit: " + commit.getName());
                    sender.sendMessage("Message: " + data.get("message"));
                    sender.sendMessage("Contributors: " + data.get("contributors"));
                    fileReader.close();
                    return true;
                } catch (FileNotFoundException e) {
                    sender.sendMessage("Error loading commit data",
                    "File not found, please report to an admin or someoneorderedatransfem");
                    sender.sendMessage(e.toString());
                    e.printStackTrace();
                    return true;
                } catch (IOException e) {
                    sender.sendMessage("Error loading commit data",
                    "IO Exception, please report to an admin or someoneorderedatransfem");
                    sender.sendMessage(e.toString());
                    e.printStackTrace();
                    return true;
                }
            }
            if (args[1].equalsIgnoreCase("set-message")) {
                File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
                File commit = new File(commitFolder, "commit-" + ((Player) sender).getWorld().getName() + ".yml");
                if (!commit.exists()) {
                    sender.sendMessage("No commit found");
                    return true;
                }
                Yaml yaml = new Yaml();
                try (FileReader fileReader = new FileReader(commit)) {
                    Map<String, Object> data = yaml.load(fileReader);
                    data.put("message", args[2]);
                    yaml.dump(data, new FileWriter(commit));
                    sender.sendMessage("Message set to " + args[2]);
                    fileReader.close();
                    return true;
                } catch (FileNotFoundException e) {
                    sender.sendMessage("Error loading commit data",
                    "File not found, please report to an admin or someoneorderedatransfem");
                    sender.sendMessage(e.toString());
                    e.printStackTrace();
                } catch (IOException e) {
                    sender.sendMessage("Error loading commit data",
                    "IO Exception, please report to an admin or someoneorderedatransfem");
                    sender.sendMessage(e.toString());
                    e.printStackTrace();
                }
            }
            if (args[1].equalsIgnoreCase("cancel-commit") && sender.hasPermission("gitcraft.commit")) {
                File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
                File commit = new File(commitFolder, "commit-" + ((Player) sender).getWorld().getName() + ".yml");
                if (!commit.exists()) {
                    sender.sendMessage("No commit found");
                    return true;
                }
                if ()
                commit.delete();
                sender.sendMessage("Commit cancelled");
                return true;
            }
                
        }
        return false;
    }
}