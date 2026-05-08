package ua.xani4ka.xanisethome;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class HomeService {
    public enum AddHomeResult {
        SUCCESS,
        ALREADY_EXISTS,
        LIMIT_REACHED
    }

    private final XaniSetHomePlugin plugin;
    private final Settings settings;
    private final HomeStorage storage;
    private final Map<UUID, List<Home>> homes;

    public HomeService(XaniSetHomePlugin plugin, Settings settings, HomeStorage storage) {
        this.plugin = plugin;
        this.settings = settings;
        this.storage = storage;
        this.homes = new ConcurrentHashMap<>(storage.loadHomes());
    }

    public int getCooldownSeconds() {
        return this.settings.getCooldownSeconds();
    }

    public boolean isCancelOnMove() {
        return this.settings.isCancelOnMove();
    }

    public int getMaxHomes(Player player) {
        return this.settings.getMaxHomes(player);
    }

    public boolean isBlockedSetHomeWorld(String worldName) {
        return this.settings.isBlockedSetHomeWorld(worldName);
    }

    public synchronized Home getHome(Player player, String name) {
        return getHomeByUUID(player.getUniqueId(), name);
    }

    public synchronized Home getHomeByUUID(UUID uuid, String name) {
        for (Home home : this.homes.getOrDefault(uuid, List.of())) {
            if (home.getName().equalsIgnoreCase(name)) {
                return home;
            }
        }
        return null;
    }

    public synchronized List<Home> getHomes(Player player) {
        return getHomesByUUID(player.getUniqueId());
    }

    public synchronized List<Home> getHomesByUUID(UUID uuid) {
        return List.copyOf(this.homes.getOrDefault(uuid, List.of()));
    }

    public synchronized AddHomeResult addHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        List<Home> playerHomes = new ArrayList<>(this.homes.getOrDefault(uuid, new ArrayList<>()));

        for (Home home : playerHomes) {
            if (home.getName().equalsIgnoreCase(name)) {
                return AddHomeResult.ALREADY_EXISTS;
            }
        }

        if (playerHomes.size() >= getMaxHomes(player)) {
            return AddHomeResult.LIMIT_REACHED;
        }

        playerHomes.add(Home.fromLocation(name, player.getLocation()));
        this.homes.put(uuid, playerHomes);
        saveAfterMutation();
        return AddHomeResult.SUCCESS;
    }

    public synchronized boolean removeHome(Player player, String name) {
        return removeHomeByUUID(player.getUniqueId(), name);
    }

    public synchronized boolean removeHomeByUUID(UUID uuid, String name) {
        List<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null) {
            return false;
        }

        Iterator<Home> iterator = playerHomes.iterator();
        while (iterator.hasNext()) {
            Home home = iterator.next();
            if (!home.getName().equalsIgnoreCase(name)) {
                continue;
            }

            iterator.remove();
            if (playerHomes.isEmpty()) {
                this.homes.remove(uuid);
            }
            saveAfterMutation();
            return true;
        }
        return false;
    }

    public synchronized void saveHomes() {
        this.storage.saveHomes(copyHomes());
    }

    public synchronized void reloadHomes() {
        this.homes.clear();
        this.homes.putAll(this.storage.loadHomes());
    }

    private void saveHomesAsync() {
        this.storage.saveHomesAsync(copyHomes());
    }

    private void saveAfterMutation() {
        String strategy = this.settings.getSaveStrategy();
        switch (strategy.toLowerCase(Locale.ROOT)) {
            case "async-immediate" -> saveHomesAsync();
            case "sync-and-async" -> {
                saveHomes();
                saveHomesAsync();
            }
            case "sync-immediate" -> saveHomes();
            default -> {
                this.plugin.getLogger().warning("Unknown save-strategy '" + strategy + "'. Falling back to sync-immediate.");
                saveHomes();
            }
        }
    }

    private synchronized Map<UUID, List<Home>> copyHomes() {
        Map<UUID, List<Home>> snapshot = new ConcurrentHashMap<>();
        for (Map.Entry<UUID, List<Home>> entry : this.homes.entrySet()) {
            snapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return snapshot;
    }
}
