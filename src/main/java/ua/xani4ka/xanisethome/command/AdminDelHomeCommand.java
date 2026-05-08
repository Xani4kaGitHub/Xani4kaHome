package ua.xani4ka.xanisethome.command;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ua.xani4ka.xanisethome.HomeService;
import ua.xani4ka.xanisethome.MessageManager;

public final class AdminDelHomeCommand implements CommandExecutor {
    private final HomeService homeService;
    private final MessageManager messageManager;

    public AdminDelHomeCommand(HomeService homeService, MessageManager messageManager) {
        this.homeService = homeService;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xanisethome.admin.delhome")) {
            sender.sendMessage(this.messageManager.get("error-no-permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(this.messageManager.get("admindelhome-usage"));
            return true;
        }

        String playerName = args[0];
        String homeName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            sender.sendMessage(this.messageManager.get("admindelhome-player-not-found", "player", playerName));
            return true;
        }

        UUID targetUuid = targetPlayer.getUniqueId();
        if (this.homeService.getHomesByUUID(targetUuid).isEmpty()) {
            sender.sendMessage(this.messageManager.get("admindelhome-no-homes", "player", playerName));
            return true;
        }

        if (this.homeService.getHomeByUUID(targetUuid, homeName) == null) {
            sender.sendMessage(this.messageManager.get("admindelhome-home-not-found", "player", playerName, "home", homeName));
            return true;
        }

        if (this.homeService.removeHomeByUUID(targetUuid, homeName)) {
            sender.sendMessage(this.messageManager.get("admindelhome-success", "player", playerName, "home", homeName));
        }
        return true;
    }
}
