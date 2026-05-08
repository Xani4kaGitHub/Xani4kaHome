package ua.xani4ka.xanisethome.command;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ua.xani4ka.xanisethome.Home;
import ua.xani4ka.xanisethome.HomeService;
import ua.xani4ka.xanisethome.MessageManager;

public final class AdminListHomesCommand implements CommandExecutor {
    private final HomeService homeService;
    private final MessageManager messageManager;

    public AdminListHomesCommand(HomeService homeService, MessageManager messageManager) {
        this.homeService = homeService;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xanisethome.admin.listhomes")) {
            sender.sendMessage(this.messageManager.get("error-no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(this.messageManager.get("adminlisthomes-usage"));
            return true;
        }

        String playerName = args[0];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            sender.sendMessage(this.messageManager.get("adminlisthomes-player-not-found", "player", playerName));
            return true;
        }

        UUID targetUuid = targetPlayer.getUniqueId();
        List<Home> homes = this.homeService.getHomesByUUID(targetUuid);
        if (homes.isEmpty()) {
            sender.sendMessage(this.messageManager.get("adminlisthomes-no-homes", "player", playerName));
            return true;
        }

        sender.sendMessage(this.messageManager.get("adminlisthomes-header", "player", playerName, "count", String.valueOf(homes.size())));
        for (Home home : homes) {
            sender.sendMessage(
                this.messageManager.get(
                    "adminlisthomes-entry",
                    "name", home.getName(),
                    "world", home.getWorldName(),
                    "x", String.format(Locale.US, "%.2f", home.getX()),
                    "y", String.format(Locale.US, "%.2f", home.getY()),
                    "z", String.format(Locale.US, "%.2f", home.getZ())
                )
            );
        }
        return true;
    }
}
