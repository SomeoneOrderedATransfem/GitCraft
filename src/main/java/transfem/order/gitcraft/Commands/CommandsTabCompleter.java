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
import java.util.Map;

import transfem.order.gitcraft.GitCraft;

public class CommandsTabCompleter implements TabCompleter {

    private final File commitFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;

        if (args.length == 1) {
            return getPrimaryCommandSuggestions(player);
        } else if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "config":
                    return getConfigCommandSuggestions(player, args);
                case "view":
                    return getCommitList();
                case "accept-commit":
                case "reject-commit":
                    return getCommitList();
                case "commit":
                    return getCommitSubCommandSuggestions(args);
            }
        }

        return null;
    }

    private List<String> getPrimaryCommandSuggestions(Player player) {
        if (player.hasPermission("gitcraft.manage")) {
            return Arrays.asList("help", "config", "view", "accept-commit", "reject-commit", "commit", "checkout");
        } else if (player.hasPermission("gitcraft.commit.manage")) {
            return Arrays.asList("help", "view", "accept-commit", "reject-commit", "commit", "checkout");
        } else if (player.hasPermission("gitcraft.commit")) {
            return Arrays.asList("help", "view", "commit", "checkout");
        } else if (player.hasPermission("gitcraft.command")) {
            return Arrays.asList("help", "view");
        }
        return null;
    }

    private List<String> getConfigCommandSuggestions(Player player, String[] args) {
        if (!player.hasPermission("gitcraft.manage")) return null;

        switch (args.length) {
            case 2:
                return Arrays.asList("set-world", "set-chunk-size", "view", "set-return-location");
            case 3:
                if (args[1].equalsIgnoreCase("set-world")) {
                    return getWorldList();
                }
                break;
        }
        return null;
    }

    private List<String> getCommitSubCommandSuggestions(String[] args) {
        switch (args.length) {
            case 2:
                return Arrays.asList("set-message", "view", "contributor", "cancel-commit");
            case 3:
                if (args[1].equalsIgnoreCase("contributor")) {
                    return Arrays.asList("add", "remove", "list");
                }
                break;
            case 4:
                if (args[1].equalsIgnoreCase("contributor")) {
                    if (args[2].equalsIgnoreCase("add")) {
                        return getPlayerListToAdd();
                    } else if (args[2].equalsIgnoreCase("remove")) {
                        return getPlayerListToRemove();
                    }
                }
                break;
        }
        return null;
    }

    private List<String> getCommitList() {
        List<String> commitList = new ArrayList<>();
        File[] files = commitFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                commitList.add(file.getName().replace(".yml", ""));
            }
        }
        return commitList;
    }

    private List<String> getWorldList() {
        List<String> worldList = new ArrayList<>();
        for (org.bukkit.World world : GitCraft.getInstance().getServer().getWorlds()) {
            worldList.add(world.getName());
        }
        return worldList;
    }

    private List<String> getPlayerListToAdd() {
        List<String> playerList = new ArrayList<>();
        Yaml yaml = new Yaml();
        File commitFile = new File(commitFolder, GitCraft.getInstance().getServer().getWorlds().get(0).getName() + ".yml");

        if (commitFile.exists()) {
            try (FileReader reader = new FileReader(commitFile)) {
                Map<String, Object> data = yaml.load(reader);
                List<String> contributors = (List<String>) data.get("contributors");

                for (Player onlinePlayer : GitCraft.getInstance().getServer().getOnlinePlayers()) {
                    if (!contributors.contains(onlinePlayer.getUniqueId().toString())) {
                        playerList.add(onlinePlayer.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return playerList;
    }

    private List<String> getPlayerListToRemove() {
        List<String> playerList = new ArrayList<>();
        Yaml yaml = new Yaml();
        File commitFile = new File(commitFolder, GitCraft.getInstance().getServer().getWorlds().get(0).getName() + ".yml");

        if (commitFile.exists()) {
            try (FileReader reader = new FileReader(commitFile)) {
                Map<String, Object> data = yaml.load(reader);
                List<String> contributors = (List<String>) data.get("contributors");

                for (String uuid : contributors) {
                    playerList.add(GitCraft.getInstance().getServer().getOfflinePlayer(uuid).getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return playerList;
    }
}
