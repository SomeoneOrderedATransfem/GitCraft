package transfem.order.gitcraft.Commands;

import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import transfem.order.gitcraft.GitCraft;

public class CommandsTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList("view", "return", "config", "checkout", "commit", "approve", "reject");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getMatchingStrings(args[0], COMMANDS);
        }

        if (args[0].equalsIgnoreCase("config")) {
            if (args.length == 2) {
                return getMatchingStrings(args[1], Arrays.asList("set", "permission"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("permission")) {
                return getMatchingStrings(args[2], Arrays.asList("give", "remove"));
            }
        }

        if (args[0].equalsIgnoreCase("commit")) {
            if (args.length == 2) {
                return getCommitNames();
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("manage")) {
                return getMatchingStrings(args[2], Arrays.asList("add", "remove", "delete"));
            }
        }

        if (args[0].equalsIgnoreCase("approve") || args[0].equalsIgnoreCase("reject")) {
            if (args.length == 2) {
                return getCommitNames();
            }
        }

        return new ArrayList<>();
    }

    private List<String> getCommitNames() {
        List<String> commitNames = new ArrayList<>();
        File commitsFolder = new File(GitCraft.getInstance().getDataFolder(), "commits");

        if (commitsFolder.exists() && commitsFolder.isDirectory()) {
            for (File file : commitsFolder.listFiles((dir, name) -> name.endsWith(".yml"))) {
                commitNames.add(file.getName().replace(".yml", ""));
            }
        }
        return commitNames;
    }

    private List<String> getMatchingStrings(String prefix, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
    }
}
