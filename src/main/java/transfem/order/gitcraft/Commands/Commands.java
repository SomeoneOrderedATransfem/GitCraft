package transfem.order.gitcraft.Commands;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.WorldCreator;

import org.jetbrains.annotations.NotNull;

import transfem.order.gitcraft.GitCraft;
import transfem.order.gitcraft.util.Config;
import transfem.order.gitcraft.util.GitCraftChunkGenerator;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands implements CommandExecutor {

    // Define permissions
    private static final String PERMISSION_VIEW = "gitcraft.view";
    private static final String PERMISSION_RETURN = PERMISSION_VIEW;
    private static final String PERMISSION_CONFIG = "gitcraft.config";
    private static final String PERMISSION_CHECKOUT = "gitcraft.checkout";
    private static final String PERMISSION_COMMIT = PERMISSION_CHECKOUT;
    private static final String PERMISSION_APPROVE = "gitcraft.approve";
    private static final String PERMISSION_REJECT = PERMISSION_APPROVE;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return handleHelp(sender);
        }

        switch (args[0].toLowerCase()) {
            case "view":
                return handleView(sender, args);
            case "return":
                return handleReturn(sender);
            case "config":
                return handleConfig(sender, args);
            case "checkout":
                return handleCheckout(sender);
            case "commit":
                return handleCommit(sender, args);
            case "approve":
                return handleApprove(sender, args);
            case "reject":
                return handleReject(sender, args);
            default:
                return handleHelp(sender);
        }
    }

    private boolean handleView(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_VIEW)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        File commits = new File(GitCraft.getInstance().getDataFolder(), "commits");

        if (args.length == 1) {
            for (File file : commits.listFiles((dir, name) -> name.endsWith(".yml"))) {
                sender.sendMessage(file.getName().replace(".yml", ""));
            }
            return true;
        }

        File commit = new File(commits, args[1] + ".yml");
        File inventoryDir = new File(GitCraft.getInstance().getDataFolder(), "inventory");
        File invFile = new File(inventoryDir, player.getUniqueId() + ".yml");
        if (!commit.exists()) {
            return true;
        }
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(commit)) {
            Map<String, Object> data = yaml.load(reader);

            World world = GitCraft.getInstance().getServer().getWorld(data.get("world").toString());
            savePlayerInventory(player, invFile);

            teleportPlayerToCommitWorld(player, data, world);

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return true;
    }

    private void savePlayerInventory(Player player, File invFile) {
        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(invFile)) {
            Map<String, Object> inventoryData = new HashMap<>();
            for (int i = 0; i < 36; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null) {
                    inventoryData.put(String.valueOf(i), item.serialize());
                }
            }
            yaml.dump(inventoryData, writer);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void teleportPlayerToCommitWorld(Player player, Map<String, Object> data, World world) {
        player.teleport(world.getSpawnLocation());
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);

        if (data.get("author").equals(player.getUniqueId().toString()) || ((List<String>) data.get("contributors")).contains(player.getUniqueId().toString())) {
            player.setGameMode(GameMode.CREATIVE);
        }
        player.setFlying(true);
    }

    private boolean handleReturn(@NotNull CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RETURN)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        sender.sendMessage("§3Sending you back to " + Config.getReturnLocation().getWorld().getName());

        File invFile = new File(GitCraft.getInstance().getDataFolder(), "inventory/" + player.getUniqueId() + ".yml");

        try (FileReader reader = new FileReader(invFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            player.getInventory().setContents(deserializeInventory(data));
            invFile.delete();
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(Config.getReturnLocation());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return true;
    }

    private boolean handleConfig(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_CONFIG)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 1) {
            sender.sendMessage("§3Current Config",
                    "§3World Name: §f" + Config.getWorld(),
                    "§3Chunk Size: §f" + Config.getRadius(),
                    "§3Return Location: §f" + Config.getReturnLocation());
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "set":
                return handleConfigSet(sender, args);
            case "permission":
                return handleConfigPermission(sender, args);
            default:
                return false;
        }
    }

    private boolean handleConfigSet(@NotNull CommandSender sender, String[] args) {
        if (args.length != 4 && !args[2].equalsIgnoreCase("returnLocation")) {
            sender.sendMessage("§cUsage: /gitcraft config set <path> <value>");
            return true;
        }
        if (args[2].equalsIgnoreCase("returnLocation")) {

            Config.set(args[2] + ".x", ((Player) sender).getLocation().getX());
            Config.set(args[2] + ".y", ((Player) sender).getLocation().getY());
            Config.set(args[2] + ".z", ((Player) sender).getLocation().getZ());
            sender.sendMessage("§3Set " + args[2] + " to " + args[3]);
            return true;
        }
        Config.set(args[2], args[3]);
        sender.sendMessage("§3Set " + args[2] + " to " + args[3]);
        return true;
    }

    private boolean handleConfigPermission(@NotNull CommandSender sender, String[] args) {
        if (args.length != 5) {
            sender.sendMessage("§cUsage: /gitcraft config permission <give|remove> <permission> <player>");
            return true;
        }

        switch (args[2].toLowerCase()) {
            case "give":
                sender.addAttachment(GitCraft.getInstance()).setPermission("gitcraft." + args[3], true);
                sender.sendMessage("§3Gave " + args[4] + " permission gitcraft." + args[3]);
                break;
            case "remove":
                sender.addAttachment(GitCraft.getInstance()).setPermission("gitcraft." + args[3], false);
                sender.sendMessage("§3Removed " + args[4] + " permission gitcraft." + args[3]);
                break;
            default:
                sender.sendMessage("§cUsage: /gitcraft config permission <give|remove> <permission> <player>");
                break;
        }
        return true;
    }

    private boolean handleCheckout(@NotNull CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_CHECKOUT)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        File commits = new File(GitCraft.getInstance().getDataFolder(), "commits");
        File commit = new File(commits, player.getWorld().getName() + ".yml");

        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(commit)) {
            World world = createCommitWorld(player);

            Map<String, Object> data = new HashMap<>();
            data.put("world", world.getName());
            data.put("author", player.getUniqueId().toString());
            data.put("contributors", List.of(player.getUniqueId().toString()));
            data.put("chunks", List.of());
            data.put("minY", 256);
            data.put("maxY", -64);

            yaml.dump(data, writer);
            copyWorldChunks(player, world);
            player.teleport(world.getSpawnLocation());
            player.sendMessage("§3Checked out world to commit " + player.getWorld().getName());

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return true;
    }

    private World createCommitWorld(Player player) {
        WorldCreator creator = new WorldCreator("commit-" + System.currentTimeMillis()).generator(new GitCraftChunkGenerator());
        return creator.createWorld();
    }

    private void copyWorldChunks(Player player, World world) {
        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "//pos1 " + (player.getX() - 16 * Config.getRadius()) + ",-64," + (player.getZ() - 16 * Config.getRadius()));
        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "//pos2 " + (player.getX() + 16 * Config.getRadius()) + ",256," + (player.getZ() + 16 * Config.getRadius()));
        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "//world " + player.getWorld().getName());
        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "//copy");
        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "//world " + world.getName());
        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "//paste");
        world.setSpawnLocation(new Location(world, player.getX(), player.getY(), player.getZ()));
    }

    private boolean handleReject(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_REJECT)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /gitcraft reject <commit> <reason>");
            return true;
        }

        File commits = new File(GitCraft.getInstance().getDataFolder(), "commits");
        File commit = new File(commits, args[1] + ".yml");

        if (commit.exists()) {
            commit.delete();
        }

        new File(GitCraft.getInstance().getDataFolder().getParentFile().getParentFile(), args[1]).delete();
        return true;
    }

    private boolean handleApprove(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_APPROVE)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /gitcraft approve <commit>");
            return true;
        }

        File commits = new File(GitCraft.getInstance().getDataFolder(), "commits");
        File commit = new File(commits, args[1] + ".yml");

        if (!commit.exists()) {
            return true;
        }

        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(commit)) {
            Map<String, Object> data = yaml.load(reader);
            copyCommitToMainWorld(sender, data);

            commit.delete();
            new File(GitCraft.getInstance().getDataFolder().getParentFile().getParentFile(), args[1]).delete();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return true;
    }

    private void copyCommitToMainWorld(CommandSender sender, Map<String, Object> data) {
        sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "//world " + data.get("world"));
        sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "//pos1 " + data.get("minX") + ", " + data.get("minY") + ", " + data.get("minZ"));
        sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "//pos2 " + data.get("maxX") + ", " + data.get("maxY") + ", " + data.get("maxZ"));
        sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "//copy");
        sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "//world " + Config.getReturnLocation().getWorld().getName());
        sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "//paste");
    }

    private boolean handleCommit(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_COMMIT)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /gitcraft commit manage <commit> <subcommand> <...args>");
            return true;
        }

        if ("manage".equalsIgnoreCase(args[1])) {
            return handleCommitManage(sender, args);
        }

        return false;
    }

    private boolean handleCommitManage(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /gitcraft commit manage <commit> <subcommand> <...args>");
            return true;
        }

        File commits = new File(GitCraft.getInstance().getDataFolder(), "commits");
        File commit = new File(commits, args[2] + ".yml");

        if (!commit.exists()) {
            return true;
        }

        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(commit)) {
            Map<String, Object> data = yaml.load(reader);

            switch (args[3].toLowerCase()) {
                case "add":
                    return handleCommitAdd(sender, args, commit, data, yaml);
                case "remove":
                    return handleCommitRemove(sender, args, commit, data, yaml);
                case "delete":
                    return handleCommitDelete(sender, commit, args);
                default:
                    sender.sendMessage("§cUsage: /gitcraft commit manage <commit> <add|remove|delete> <...args>");
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return true;
    }

    private boolean handleCommitAdd(CommandSender sender, String[] args, File commit, Map<String, Object> data, Yaml yaml) {
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /gitcraft commit manage <commit> add <player>");
            return true;
        }

        List<String> contributors = (List<String>) data.get("contributors");
        contributors.add(args[4]);
        data.put("contributors", contributors);

        try (FileWriter writer = new FileWriter(commit)) {
            yaml.dump(data, writer);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        sender.sendMessage("§3Added " + args[4] + " to the contributors of " + args[2]);
        return true;
    }

    private boolean handleCommitRemove(CommandSender sender, String[] args, File commit, Map<String, Object> data, Yaml yaml) {
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /gitcraft commit manage <commit> remove <player>");
            return true;
        }

        List<String> contributors = (List<String>) data.get("contributors");
        contributors.remove(args[4]);
        data.put("contributors", contributors);

        try (FileWriter writer = new FileWriter(commit)) {
            yaml.dump(data, writer);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        sender.sendMessage("§3Removed " + args[4] + " from the contributors of " + args[2]);
        return true;
    }

    private boolean handleCommitDelete(CommandSender sender, File commit, String[] args) {
        commit.delete();
        new File(GitCraft.getInstance().getDataFolder().getParentFile().getParentFile(), args[2]).delete();
        sender.sendMessage("§3Deleted commit " + args[2]);
        return true;
    }

    private boolean handleHelp(@NotNull CommandSender sender) {
        sender.sendMessage("§3Gitcraft Help",
                "§3/gitcraft help §f- Show this help message",
                "§3/gitcraft view §f- View all commit worlds",
                "§3/gitcraft view §6<world> §f- View the specified world",
                "§3/gitcraft return §f- Return to the return location");

        if (sender.hasPermission(PERMISSION_CONFIG)) {
            sender.sendMessage("§e/gitcraft config §f- Show the Current Config for Gitcraft",
                    "§3/gitcraft config set §6<path> <value> §f- Set a value in the config",
                    "§3/gitcraft config permission <give|remove> <permission> <player> §f- Save the current config");
        }

        if (sender.hasPermission(PERMISSION_COMMIT)) {
            sender.sendMessage("§3/gitcraft checkout - Checkout the current world to a new commit world",
                    "§3/gitcraft commit manage §6<commit> <subcommand> <...args>§f- Manage the details of the specified commit");
        }

        if (sender.hasPermission(PERMISSION_APPROVE)) {
            sender.sendMessage("§3/gitcraft approve §6<commit> §f- Approve the specified commit",
                    "§3/gitcraft reject §6<commit> <reason> §f- Reject the specified commit");
        }

        return true;
    }

    private ItemStack[] deserializeInventory(Map<String, Object> data) {
        ItemStack[] inventory = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            Map<String, Object> itemData = (Map<String, Object>) data.get(String.valueOf(i));
            if (itemData != null) {
                inventory[i] = ItemStack.deserialize(itemData);
            }
        }
        return inventory;
    }
}
