package ua.xani4ka.xanisethome.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.xani4ka.xanisethome.HomeService;
import ua.xani4ka.xanisethome.MessageManager;
import ua.xani4ka.xanisethome.SoundManager;

public final class DelHomeCommand implements CommandExecutor {
    private final HomeService homeService;
    private final MessageManager messageManager;
    private final SoundManager soundManager;

    public DelHomeCommand(HomeService homeService, MessageManager messageManager, SoundManager soundManager) {
        this.homeService = homeService;
        this.messageManager = messageManager;
        this.soundManager = soundManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.messageManager.get("error-player-only"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(this.messageManager.get("error-invalid-arguments"));
            return true;
        }

        String name = args[0];
        if (this.homeService.removeHome(player, name)) {
            player.sendMessage(this.messageManager.get("delhome-success", "name", name));
        } else {
            player.sendMessage(this.messageManager.get("error-home-not-found"));
            this.soundManager.play(player, "generic-error");
        }
        return true;
    }
}
