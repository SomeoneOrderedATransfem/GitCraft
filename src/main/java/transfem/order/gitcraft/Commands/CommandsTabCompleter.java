package transfem.order.gitcraft.Commands;



import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import transfem.order.gitcraft.GitCraft;

/*
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
 */

public class CommandsTabCompleter implements TabCompleter {

    private final File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        if (args.length == 0) {
            if (sender.hasPermission("gitcraft.manage")) {
                return Arrays.asList("help", "config", "view", "accept-commit", "reject-commit", "commit", "checkout");
            }
            if (sender.hasPermission("gitcraft.commit.manage")) {
                return Arrays.asList("help", "view", "accept-commit", "reject-commit", "commit", "checkout");
            }
            if (sender.hasPermission("gitcraft.commit")) {
                return Arrays.asList("help", "view", "commit", "checkout");
            }
            if (sender.hasPermission("gitcraft.commands")) {
                return Arrays.asList("help", "view");
            }
        } 
        if (sender.hasPermission("gitcraft.manage") && args[0].equalsIgnoreCase("config")) {
            if (args.length == 1) {
                return Arrays.asList("set-world", "set-chunk-size", "view");
            }
            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("set-world")) {
                    return Arrays.asList("world");
                }
                if (args[1].equalsIgnoreCase("set-chunk-size")) {
                    return Arrays.asList("size");
                }
                if (args[1].equalsIgnoreCase("view")) {
                    return null;
                }
                if (args[1].equalsIgnoreCase("set-return-location")) {
                    return null;
                }
            }
        }
        if (args[0].equalsIgnoreCase("view")) {
            if (args.length == 1) {
                List<String> temp = new ArrayList<>();
                for (File file : commitFolder.listFiles()) {
                    temp.add(file.getName().split(".")[0]);
                }
                return temp;
            }
        }
        if (args[0].equalsIgnoreCase("accept-commit")) {
            if (args.length == 1) {
                List<String> temp = new ArrayList<>();
                for (File file : commitFolder.listFiles()) {
                    temp.add(file.getName().split(".")[0]);
                }
                return temp;
            }
        }
        if (args[0].equalsIgnoreCase("reject-commit")) {
            if (args.length == 1) {
                List<String> temp = new ArrayList<>();
                for (File file : commitFolder.listFiles()) {
                    temp.add(file.getName().split(".")[0]);
                }
                return temp;
            }
            if (args.length == 2) {
                return null;
            }
        }
        if (args[0].equalsIgnoreCase("commit")) {
            if (args.length == 1) {
                return Arrays.asList("set-message", "view", "contributor", "cancel-commit");
            }
            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("set-message")) {
                    return Arrays.asList("message");
                }
                if (args[1].equalsIgnoreCase("contributor")) {
                    return Arrays.asList("add", "remove", "list");
                }
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("contributor")) {
                    if (args[2].equalsIgnoreCase("add")) {
                        List<String> temp = new ArrayList<>();
                        Yaml yaml = new Yaml();
                        try (FileReader reader = new FileReader(new File(commitFolder, ((Player) sender).getWorld().getName()+".yml"))) {
                            for (Player player : GitCraft.getInstance().getServer().getOnlinePlayers()) {
                                if (!((List<String>) yaml.load(reader)).contains(player.getUniqueId().toString())) {
                                    temp.add(player.getName());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return temp;
                    }
                    if (args[2].equalsIgnoreCase("remove")) {
                        List<String> temp = new ArrayList<>();
                        Yaml yaml = new Yaml();
                        try (FileReader reader = new FileReader(new File(commitFolder, ((Player) sender).getWorld().getName()+".yml"))) {
                            for (String uuid : (List<String>) yaml.load(reader)) {
                                temp.add(sender.getServer().getOfflinePlayer(uuid).getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return temp;
                    }
                    if (args[2].equalsIgnoreCase("list")) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}