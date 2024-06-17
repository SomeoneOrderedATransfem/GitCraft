package transfem.order.gitcraft.Commands;



import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Arrays;
import java.io.File;

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
        if (sender instanceof Player) {
            if (args.length == 1) {
                if (sender.hasPermission("gitcraft.manage")) {
                    return Arrays.asList("help", "config", "view", "accept-commit", "reject-commit", "checkout", "commit");
                }
                if (sender.hasPermission("gitcraft.commit.manage")) {
                    return Arrays.asList("help", "commit", "view", "accept-commit", "reject-commit", "checkout");
                }
                if (sender.hasPermission("gitcraft.commit")){
                    return Arrays.asList("help", "commit", "view", "checkout");
                }
                if (sender.hasPermission("gitcraft.command")){
                    return Arrays.asList("help", "view", "commit");
                }
                return Arrays.asList("help", "config", "view", "accept-commit", "reject-commit", "checkout", "commit");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("config")) {
                    return Arrays.asList("set-world", "set-chunk-size", "view");
                } else if (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("accept-commit") || args[0].equalsIgnoreCase("reject-commit")) {
                    String[] worlds = new String[commitFolder.listFiles().length];
                    for (int i = 0; i < commitFolder.listFiles().length; i++) {
                        worlds[i] = commitFolder.listFiles()[i].getName().replace(".json", "");
                    }
                    return Arrays.asList(worlds);
                } else if (args[0].equalsIgnoreCase("commit")) {
                    return Arrays.asList("set-message", "view", "contributor", "cancel-commit");
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("config")) {
                    if (args[1].equalsIgnoreCase("set-world")) {
                        String[] worlds = new String[commitFolder.listFiles().length];
                        for (int i = 0; i < commitFolder.listFiles().length; i++) {
                            worlds[i] = commitFolder.listFiles()[i].getName().replace(".json", "");
                        }
                        return Arrays.asList(worlds);
                    } else if (args[1].equalsIgnoreCase("set-chunk-size")) {
                        return null;
                    }
                } else if (args[0].equalsIgnoreCase("commit")) {
                    if (args[1].equalsIgnoreCase("set-message")) {
                        return Arrays.asList("<message>");
                    } else if (args[1].equalsIgnoreCase("contributor")) {
                        return Arrays.asList("add", "remove", "list");
                    }
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("commit")) {
                    if (args[1].equalsIgnoreCase("contributor")) {
                        if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove")) {
                            String[] players = new String[GitCraft.getInstance().getServer().getOnlinePlayers().size()];
                            for (int i = 0; i < GitCraft.getInstance().getServer().getOnlinePlayers().size(); i++) {
                                players[i] = GitCraft.getInstance().getServer().getOnlinePlayers().toArray(new Player[0])[i].getName();
                            }
                            return Arrays.asList(players);
                        }
                    }
                }
            }
        }
        return null;
    }
}