package transfem.order.gitcraft.Commands;

import transfem.order.gitcraft.GitCraft;
import transfem.order.gitcraft.util.Config;
import transfem.order.gitcraft.util.GitCraftChunkGenerator;
import transfem.order.gitcraft.Listeners.PlayerListeners;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.GameMode;

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
    @SuppressWarnings("unchecked")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("gitcraft.command") && (args.length == 0 || args[0].equalsIgnoreCase("help"))) {
            sender.sendMessage("\u00A73GitCraft Commands:",
            "\u00A73/gitcraft help - \u00A7fDisplays this message",
            "\u00A73/gitcraft config set-world <world> - \u00A7fSet the world to use for GitCraft",
            "\u00A73/gitcraft config set-world - \u00A7fSet the world to use for GitCraft to this world",
            "\u00A73/gitcraft config set-chunk-radius <radius> - \u00A7fSet the chunk radius for GitCraft",
            "\u00A73/gitcraft config set-return-location - \u00A7fSet the return location for GitCraft",
            "\u00A73/gitcraft config view - \u00A7fView the current configuration of GitCraft",
            "\u00A73/gitcraft config - \u00A7fView the configuration of GitCraft",
            "\u00A73/gitcraft view - \u00A7fList all worlds",
            "\u00A73/gitcraft view <world> - \u00A7fView the world of the specified commit",
            "\u00A73/gitcraft return - \u00A7fReturn to the main world",
            "\u00A73/gitcraft accept-commit <world> - \u00A7fAccept the commit of the specified world",
            "\u00A73/gitcraft reject-commit <world> <reason> - \u00A7fReject the commit of the specified world",
            "\u00A73/gitcraft checkout - \u00A7fCheckout the current world to a new commit",
            "\u00A73/gitcraft commit set-message <message> - \u00A7fSet the commit message",
            "\u00A73/gitcraft commit view - \u00A7fView the current data of the commit",
            "\u00A73/gitcraft commit contributor add <player> - \u00A7fAdd a contributor to the commit",
            "\u00A73/gitcraft commit contributor remove <player> - \u00A7fRemove a contributor from the commit",
            "\u00A73/gitcraft commit contributor list - \u00A7fList all contributors of the commit",
            "\u00A73/gitcraft commit cancel-commit - \u00A7fCancel the current commit");
            return true;
        }


        if (sender.hasPermission("gitcraft.manage") && args[0].equalsIgnoreCase("config")) {


            if (args.length == 1 || args[1].equalsIgnoreCase("view")) {
                sender.sendMessage("\u00A73World: \u00A7f" + Config.getWorld());
                sender.sendMessage("\u00A73Chunk Size: \u00A7f" + Config.getRadius());
                return true;
            }


            if (args[1].equalsIgnoreCase("set-world")) {
                if (args.length == 2) {
                    if (sender instanceof Player) {
                        Config.set("worldName", ((Player) sender).getWorld().getName());
                        sender.sendMessage("\u00A73World set to \u00A7f\u00A7l" + ((Player) sender).getWorld().getName());
                        return true;
                    } else {
                        sender.sendMessage("\u00A7cYou must be a player to use this command");
                        return true;
                    }
                } else {
                    if (sender.getServer().getWorld(args[2]) == null) {
                        sender.sendMessage("\u00A7cWorld not found");
                        return true;
                    }
                    Config.set("worldName", args[2]);
                    sender.sendMessage("\u00A73World set to \u00A7f\u00A7l" + args[2]);
                    return true;
                }
            }


            if (args[1].equalsIgnoreCase("set-chunk-radius")) {
                if (args.length == 2) {
                    sender.sendMessage("Usage: /gitcraft config set-chunk-radius <radius>");
                    return true;
                }
                try {
                    int radius = Integer.parseInt(args[2]);
                    Config.set("chunkSize", radius);
                    sender.sendMessage("\u00A73Chunk radius set to \u00A7f\u00A7l" + radius);
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage("\u00A7cInvalid radius");
                    return true;
                }
            }

            if (args[1].equalsIgnoreCase("set-return-location")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command");
                    return true;
                }
                Config.setReturnLocation(((Player) sender).getLocation());
                sender.sendMessage("\u00A73Return location set to \u00A7f" + ((Player) sender).getLocation().getBlockX() + ", " + ((Player) sender).getLocation().getBlockY() + ", " + ((Player) sender).getLocation().getBlockZ());
                return true;
            }
        }


        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command");
            return true;
        }
        Player player = (Player) sender;
        File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");

        if (sender.hasPermission("gitcraft.command") && args[0].equalsIgnoreCase("view")) {
            if (args.length == 1) {
                File[] files = commitFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".yml");
                    }
                });
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> data = yaml.load(reader);
                        sender.sendMessage(file.getName().substring(0, file.getName().length() - 4) + " - " + data.get("message"));
                    } catch (Exception e) {
                        sender.sendMessage("\u00A7cError reading commit " + file.getName() +" - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return true;
            }


            File file = new File(commitFolder, args[1] + ".yml");
            if (!file.exists()) {
                sender.sendMessage("\u00A7cCommit not found");
                return true;
            }
            try (FileReader reader = new FileReader(file)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(reader);
                sender.sendMessage("\u00A73Message: \u00A7f" + data.get("message"));
            } catch (Exception e) {
                sender.sendMessage("\u00A7cError reading commit " + args[1] + " - " + e.getMessage());
                e.printStackTrace();
            }
            
        }


        if (sender.hasPermission("gitcraft.command") && args[0].equalsIgnoreCase("return")) {
            PlayerListeners.playerReturn(player);
            player.teleport(Config.getReturnLocation());
            return true;
        }


        if ((sender.hasPermission("gitcraft.commit") || sender.hasPermission("gitcraft.manage")) && args[0].equalsIgnoreCase("checkout")) {
            if (args.length == 1) {
                sender.sendMessage("Usage: /gitcraft checkout <message>");
                return true;
            }
            String id = ("" + System.currentTimeMillis());
            id = "commit-"+id.substring(id.length() - 7);
            File file = new File(commitFolder, id + ".yml");
            if (file.exists()) {
                sender.sendMessage("\u00A7cA commit already exists for this world");
                return true;
            }
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = new HashMap<>();
                    data.put("message", String.join(" ", args).substring(10));
                    data.put("contributors", new ArrayList<String>());
                    yaml.dump(data, writer);
                    writer.close();
                    WorldCreator creator = new WorldCreator(id);
                    creator.type(WorldType.FLAT);
                    creator.generator(new GitCraftChunkGenerator());
                    creator.createWorld();
                    sender.sendMessage("\u00A73World checked out to commit \u00A7f" + id);
                    // save the player's inventory to a file
                    File inventoryFile = new File(GitCraft.getInstance().getDataFolder(), "inventories/" + id + ".yml");
                    inventoryFile.createNewFile();
                    try (FileWriter inventoryWriter = new FileWriter(inventoryFile)) {
                        Yaml inventoryYaml = new Yaml();
                        inventoryYaml.dump(player.getInventory(), inventoryWriter);
                        inventoryWriter.close();
                        player.getInventory().clear();
                    }
                    try {
                        // copy all blocks in surrounding chunks to the new world
                        World world = player.getWorld();
                        int radius = Config.getRadius();
                        for (int x = -radius; x <= radius; x++) {
                            for (int z = -radius; z <= radius; z++) {
                                for (int y = -62; y <256; y++) {
                                    Chunk chunk = world.getChunkAt(player.getLocation().getBlockX() + x * 16, player.getLocation().getBlockZ() + z * 16);
                                    if (!chunk.isLoaded()) {
                                        chunk.load();
                                    }
                                    player.getWorld().getBlockAt(player.getLocation().getBlockX() + x * 16, y, player.getLocation().getBlockZ() + z * 16).setType(world.getBlockAt(player.getLocation().getBlockX() + x * 16, y, player.getLocation().getBlockZ() + z * 16).getType());
                                }
                            }
                        }
                        player.setGameMode(GameMode.CREATIVE);
                    } catch (Exception e) {
                        sender.sendMessage("\u00A7cError copying blocks - " + e.getMessage());
                        e.printStackTrace();
                    }
                    player.teleport(new Location(player.getServer().getWorld(id), player.getX(), player.getY(), player.getZ()));
                }
                sender.sendMessage("\u00A73Commit created");
            } catch (Exception e) {
                sender.sendMessage("\u00A7cError creating commit - " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }


        if ((sender.hasPermission("gitcraft.commit") || sender.hasPermission("gitcraft.manage")) && args[0].equalsIgnoreCase("commit") && player.getWorld().getName().startsWith("commit")) {
            if (args.length == 1 || args[1].equalsIgnoreCase("view")) {
                File file = new File(commitFolder, player.getWorld().getName() + ".yml");
                if (!file.exists()) {
                    sender.sendMessage("\u00A7cNo commit exists for this world");
                    return true;
                }
                try (FileReader reader = new FileReader(file)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(reader);
                    sender.sendMessage("\u00A73Message: \u00A7f" + data.get("message"));
                    sender.sendMessage("\u00A73Contributors: \u00A7f" + String.join(", ", (List<String>) data.get("contributors")));
                } catch (Exception e) {
                    sender.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("set-message")) {
                if (args.length == 2) {
                    sender.sendMessage("Usage: /gitcraft commit set-message <message>");
                    return true;
                }
                File file = new File(commitFolder, player.getWorld().getName() + ".yml");
                if (!file.exists()) {
                    sender.sendMessage("\u00A7cNo commit exists for this world");
                    return true;
                }
                // check if the player is a contributor
                try (FileReader reader = new FileReader(file)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(reader);
                    if (!((List<String>) data.get("contributors")).contains(player.getUniqueId().toString())) {
                        sender.sendMessage("\u00A7cYou are not a contributor to this commit");
                        return true;
                    }
                    reader.close();
                } catch (Exception e) {
                    sender.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
                    e.printStackTrace();
                    return true;
                }
                try (FileWriter writer = new FileWriter(file)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(new FileReader(file));
                    data.put("message", String.join(" ", args).substring(10));
                    yaml.dump(data, writer);
                    sender.sendMessage("\u00A73Message set to \u00A7f" + String.join(" ", args).substring(10));
                    writer.close();
                } catch (Exception e) {
                    sender.sendMessage("\u00A7cError setting message - " + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("contributor")) {
                if (args.length == 2 || args[2].equalsIgnoreCase("view")) {
                    File file = new File(commitFolder, player.getWorld().getName() + ".yml");
                    if (!file.exists()) {
                        sender.sendMessage("\u00A7cNo commit exists for this world");
                        return true;
                    }
                    try (FileReader reader = new FileReader(file)) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> data = yaml.load(reader);
                        sender.sendMessage("\u00A73Contributors: \u00A7f" + String.join(", ", (List<String>) data.get("contributors")));
                    } catch (Exception e) {
                        sender.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                }
                if (args[3].equalsIgnoreCase("add")) {
                    if (args.length == 3) {
                        sender.sendMessage("Usage: /gitcraft commit contributor add <player>");
                        return true;
                    }
                    File file = new File(commitFolder, player.getWorld().getName() + ".yml");
                    if (!file.exists()) {
                        sender.sendMessage("\u00A7cNo commit exists for this world");
                        return true;
                    }
                    try (FileWriter writer = new FileWriter(file)) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> data = yaml.load(new FileReader(file));
                        OfflinePlayer offlinePlayer = player.getServer().getOfflinePlayer(args[4]);
                        if (offlinePlayer.hasPlayedBefore()) {
                            if (((List<String>) data.get("contributors")).contains(offlinePlayer.getUniqueId().toString())) {
                                sender.sendMessage("\u00A7cPlayer is already a contributor");
                                return true;
                            }
                            ((List<String>) data.get("contributors")).add(offlinePlayer.getUniqueId().toString());
                            yaml.dump(data, writer);
                            sender.sendMessage("\u00A73Contributor added");
                        } else {
                            sender.sendMessage("\u00A7cPlayer not found");
                        }
                        writer.close();
                    } catch (Exception e) {
                        sender.sendMessage("\u00A7cError adding contributor - " + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                }
                if (args[3].equalsIgnoreCase("remove")) {
                    if (args.length == 3) {
                        sender.sendMessage("Usage: /gitcraft commit contributor remove <player>");
                        return true;
                    }
                    File file = new File(commitFolder, player.getWorld().getName() + ".yml");
                    if (!file.exists()) {
                        sender.sendMessage("\u00A7cNo commit exists for this world");
                        return true;
                    }
                    try (FileWriter writer = new FileWriter(file)) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> data = yaml.load(new FileReader(file));
                        OfflinePlayer offlinePlayer = player.getServer().getOfflinePlayer(args[4]);
                        if (offlinePlayer.hasPlayedBefore()) {
                            if (!((List<String>) data.get("contributors")).contains(offlinePlayer.getUniqueId().toString())) {
                                sender.sendMessage("\u00A7cPlayer is not a contributor");
                                return true;
                            }
                            ((List<String>) data.get("contributors")).remove(offlinePlayer.getUniqueId().toString());
                            yaml.dump(data, writer);
                            sender.sendMessage("\u00A73Contributor removed");
                        } else {
                            sender.sendMessage("\u00A7cPlayer not found");
                        }
                        writer.close();
                    } catch (Exception e) {
                        sender.sendMessage("\u00A7cError removing contributor - " + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            if (args[1].equalsIgnoreCase("cancel-commit")) {
                File file = new File(commitFolder, player.getWorld().getName() + ".yml");
                if (!file.exists()) {
                    sender.sendMessage("\u00A7cNo commit exists for this world");
                    return true;
                }
                Yaml yaml = new Yaml();
                try (FileReader reader = new FileReader(file)) {
                    Map<String, Object> data = yaml.load(reader);
                    if (data.get("author").equals(player.getUniqueId().toString())) {
                        sender.sendMessage("\u00A7cYou are not the author of this commit");
                        return true;
                    }
                } catch (Exception e) {
                    sender.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
                    e.printStackTrace();
                    return true;
                }
                try {
                    file.delete();
                    sender.sendMessage("\u00A73Commit cancelled");
                    player.teleport(Config.getReturnLocation());
                } catch (Exception e) {
                    sender.sendMessage("\u00A7cError cancelling commit - " + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            }
        }

        return false;
    }
}
