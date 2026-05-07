package ua.xani4ka.xanisethome;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class HomeStorage {
    private final XaniSetHomePlugin plugin;
    private final Settings settings;
    private final File homesFile;
    private final File backupFile;

    public HomeStorage(XaniSetHomePlugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        this.backupFile = new File(plugin.getDataFolder(), "homes.yml.bak");
    }

    public Map<UUID, List<Home>> loadHomes() {
        Map<UUID, List<Home>> homes = new HashMap<>();
        if (!this.homesFile.exists()) {
            return homes;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.homesFile);
        for (String rawUuid : yaml.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Skipping invalid UUID in homes.yml: " + rawUuid);
                continue;
            }

            ConfigurationSection playerSection = yaml.getConfigurationSection(rawUuid);
            if (playerSection == null) {
                continue;
            }

            List<Home> playerHomes = new ArrayList<>();
            for (String homeName : playerSection.getKeys(false)) {
                try {
                    String path = rawUuid + "." + homeName;
                    if (!yaml.isConfigurationSection(path)) {
                        this.plugin.getLogger().warning("Skipping invalid home '" + homeName + "' for " + rawUuid + ": not a section.");
                        continue;
                    }

                    String worldName = yaml.getString(path + ".world");
                    if (worldName == null || worldName.isBlank()) {
                        this.plugin.getLogger().warning("Skipping invalid home '" + homeName + "' for " + rawUuid + ": missing world.");
                        continue;
                    }

                    double x = requireNumber(yaml, path + ".x");
                    double y = requireNumber(yaml, path + ".y");
                    double z = requireNumber(yaml, path + ".z");
                    float yaw = (float) requireNumber(yaml, path + ".yaw");
                    float pitch = (float) requireNumber(yaml, path + ".pitch");
                    playerHomes.add(new Home(homeName, worldName, x, y, z, yaw, pitch));
                } catch (IllegalArgumentException exception) {
                    this.plugin.getLogger().warning("Skipping broken home '" + homeName + "' for " + rawUuid + ": " + exception.getMessage());
                } catch (Exception exception) {
                    this.plugin.getLogger().warning("Skipping broken home '" + homeName + "' for " + rawUuid + "' due to unexpected error: " + exception.getMessage());
                }
            }

            if (!playerHomes.isEmpty()) {
                homes.put(uuid, playerHomes);
            }
        }
        return homes;
    }

    public void saveHomes(Map<UUID, List<Home>> homes) {
        try {
            writeHomes(buildHomesYaml(homes).saveToString());
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Could not save homes.yml: " + exception.getMessage());
        }
    }

    public void saveHomesAsync(Map<UUID, List<Home>> homes) {
        String snapshot = buildHomesYaml(homes).saveToString();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                writeHomes(snapshot);
            } catch (IOException exception) {
                this.plugin.getLogger().severe("Could not save homes.yml asynchronously: " + exception.getMessage());
            }
        });
    }

    private YamlConfiguration buildHomesYaml(Map<UUID, List<Home>> homes) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, List<Home>> entry : homes.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Home home : entry.getValue()) {
                String path = uuid + "." + home.getName();
                yaml.set(path + ".world", home.getWorldName());
                yaml.set(path + ".x", home.getX());
                yaml.set(path + ".y", home.getY());
                yaml.set(path + ".z", home.getZ());
                yaml.set(path + ".yaw", home.getYaw());
                yaml.set(path + ".pitch", home.getPitch());
            }
        }
        return yaml;
    }

    private void writeHomes(String data) throws IOException {
        Files.createDirectories(this.homesFile.getParentFile().toPath());
        File tempFile = new File(this.homesFile.getParentFile(), this.homesFile.getName() + ".tmp");
        Files.writeString(tempFile.toPath(), data, StandardCharsets.UTF_8);

        if (this.settings.shouldWriteBackupFile() && this.homesFile.isFile()) {
            Files.copy(this.homesFile.toPath(), this.backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        Files.move(tempFile.toPath(), this.homesFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private double requireNumber(YamlConfiguration yaml, String path) {
        Object value = yaml.get(path);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("field '" + path + "' is missing or not a number");
        }
        return number.doubleValue();
    }
}
