package ua.xani4ka.xanisethome;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class HomeTabCompleter implements TabCompleter {
    private final HomeService homeService;

    public HomeTabCompleter(HomeService homeService) {
        this.homeService = homeService;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!(sender instanceof Player player) || args.length != 1) {
            return suggestions;
        }

        String input = args[0].toLowerCase();
        for (Home home : this.homeService.getHomes(player)) {
            if (home.getName().toLowerCase().startsWith(input)) {
                suggestions.add(home.getName());
            }
        }
        return suggestions;
    }
}
