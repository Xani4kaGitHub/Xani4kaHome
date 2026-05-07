package ua.xani4ka.xanisethome.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.xani4ka.xanisethome.HomeService;
import ua.xani4ka.xanisethome.MessageManager;
import ua.xani4ka.xanisethome.SoundManager;

public final class SetHomeCommand implements CommandExecutor {
    private final HomeService homeService;
    private final MessageManager messageManager;
    private final SoundManager soundManager;

    public SetHomeCommand(HomeService homeService, MessageManager messageManager, SoundManager soundManager) {
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
        if (name.contains(".")) {
            player.sendMessage(this.messageManager.get("error-home-name-invalid"));
            this.soundManager.play(player, "generic-error");
            return true;
        }

        String worldName = player.getWorld().getName();
        if (this.homeService.isBlockedSetHomeWorld(worldName)) {
            player.sendMessage(this.messageManager.get("error-sethome-world-blocked", "world", worldName));
            this.soundManager.play(player, "generic-error");
            return true;
        }

        HomeService.AddHomeResult result = this.homeService.addHome(player, name);
        switch (result) {
            case SUCCESS -> {
                player.sendMessage(this.messageManager.get("sethome-success", "name", name));
                this.soundManager.play(player, "sethome-success");
            }
            case REPLACED_IN_WORLD -> {
                player.sendMessage(this.messageManager.get("sethome-replaced-in-world", "world", player.getWorld().getName()));
                player.sendMessage(this.messageManager.get("sethome-success", "name", name));
                this.soundManager.play(player, "sethome-success");
            }
            case ALREADY_EXISTS -> {
                player.sendMessage(this.messageManager.get("sethome-already-exists"));
                this.soundManager.play(player, "generic-error");
            }
            case LIMIT_REACHED -> {
                player.sendMessage(this.messageManager.get("sethome-max-homes", "max", String.valueOf(this.homeService.getMaxHomes(player))));
                this.soundManager.play(player, "sethome-limit");
            }
        }
        return true;
    }
}
