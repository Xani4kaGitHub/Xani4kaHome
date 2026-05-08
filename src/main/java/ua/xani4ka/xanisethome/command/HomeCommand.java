package ua.xani4ka.xanisethome.command;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ua.xani4ka.xanisethome.Home;
import ua.xani4ka.xanisethome.HomeService;
import ua.xani4ka.xanisethome.MessageManager;
import ua.xani4ka.xanisethome.SoundManager;
import ua.xani4ka.xanisethome.XaniSetHomePlugin;

public final class HomeCommand implements CommandExecutor {
    private final XaniSetHomePlugin plugin;
    private final HomeService homeService;
    private final MessageManager messageManager;
    private final SoundManager soundManager;
    private final Map<UUID, BukkitRunnable> pendingTeleports = new ConcurrentHashMap<>();

    public HomeCommand(XaniSetHomePlugin plugin, HomeService homeService, MessageManager messageManager, SoundManager soundManager) {
        this.plugin = plugin;
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
        Home home = this.homeService.getHome(player, name);
        if (home == null) {
            player.sendMessage(this.messageManager.get("error-home-not-found"));
            this.soundManager.play(player, "generic-error");
            return true;
        }

        if (home.toLocation() == null) {
            player.sendMessage(this.messageManager.get("error-home-world-unavailable", "world", home.getWorldName()));
            this.soundManager.play(player, "generic-error");
            return true;
        }

        cancelPendingTeleport(player.getUniqueId());
        int cooldown = this.homeService.getCooldownSeconds();
        if (cooldown <= 0) {
            teleport(player, home, name);
            return true;
        }

        Location startLocation = player.getLocation().clone();
        BukkitRunnable task = new BukkitRunnable() {
            private int secondsLeft = cooldown;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pendingTeleports.remove(player.getUniqueId(), this);
                    cancel();
                    return;
                }

                if (secondsLeft > 0) {
                    player.showTitle(
                        Title.title(
                            Component.empty(),
                            messageManager.get("home-teleport-cooldown", "seconds", String.valueOf(secondsLeft)),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
                        )
                    );

                    if (homeService.isCancelOnMove() && hasMoved(player.getLocation(), startLocation)) {
                        player.showTitle(
                            Title.title(
                                Component.empty(),
                                messageManager.get("home-teleport-canceled"),
                                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
                            )
                        );
                        soundManager.play(player, "home-teleport-canceled");
                        pendingTeleports.remove(player.getUniqueId(), this);
                        cancel();
                        return;
                    }

                    secondsLeft--;
                    return;
                }

                teleport(player, home, name);
                pendingTeleports.remove(player.getUniqueId(), this);
                cancel();
            }
        };
        this.pendingTeleports.put(player.getUniqueId(), task);
        task.runTaskTimer(this.plugin, 0L, 20L);
        return true;
    }

    private void teleport(Player player, Home home, String name) {
        Location targetLocation = home.toLocation();
        if (targetLocation == null) {
            player.sendMessage(this.messageManager.get("error-home-world-unavailable", "world", home.getWorldName()));
            this.soundManager.play(player, "generic-error");
            return;
        }
        player.teleport(targetLocation);
        this.soundManager.play(player, "home-teleport-success");
        player.sendMessage(this.messageManager.get("home-teleport-success", "name", name));
    }

    private boolean hasMoved(Location current, Location start) {
        if (current.getWorld() == null || start.getWorld() == null) {
            return true;
        }
        return current.getX() != start.getX()
            || current.getY() != start.getY()
            || current.getZ() != start.getZ()
            || !current.getWorld().getUID().equals(start.getWorld().getUID());
    }

    private void cancelPendingTeleport(UUID playerId) {
        BukkitRunnable existingTask = this.pendingTeleports.remove(playerId);
        if (existingTask != null) {
            existingTask.cancel();
        }
    }
}
