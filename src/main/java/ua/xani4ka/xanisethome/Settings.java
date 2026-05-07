package ua.xani4ka.xanisethome;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class Settings {
    private final XaniSetHomePlugin plugin;

    public Settings(XaniSetHomePlugin plugin) {
        this.plugin = plugin;
    }

    public int getCooldownSeconds() {
        return this.plugin.getConfig().getInt("cooldown-seconds", 0);
    }

    public boolean isCancelOnMove() {
        return this.plugin.getConfig().getBoolean("cancel-on-move", false);
    }

    public int getMaxHomes(Player player) {
        ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("max-homes");
        if (section == null) {
            return this.plugin.getConfig().getInt("max-homes", 15);
        }

        int max = section.getInt("default", 15);
        for (String key : section.getKeys(false)) {
            if ("default".equalsIgnoreCase(key)) {
                continue;
            }
            if (!player.hasPermission("homeplugin.limit." + key)) {
                continue;
            }
            int value = section.getInt(key, max);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public boolean isBlockedSetHomeWorld(String worldName) {
        for (String blockedWorld : this.plugin.getConfig().getStringList("blocked-sethome-worlds")) {
            if (blockedWorld.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    public String getSaveStrategy() {
        return this.plugin.getConfig().getString("save-strategy", "sync-immediate");
    }

    public boolean shouldWriteBackupFile() {
        return this.plugin.getConfig().getBoolean("write-backup-file", true);
    }

    public boolean isLegacyImportEnabled() {
        return this.plugin.getConfig().getBoolean("legacy-import.enabled", true);
    }

    public String getLegacyImportFolder() {
        return this.plugin.getConfig().getString("legacy-import.folder", "legacy-homes");
    }

    public String getLegacyImportFile() {
        return this.plugin.getConfig().getString("legacy-import.file", "homes.yml");
    }

    public String getLanguage() {
        return this.plugin.getConfig().getString("language", "en");
    }

    public ConfigurationSection getMessageOverrides() {
        return this.plugin.getConfig().getConfigurationSection("message-overrides");
    }

    public boolean isSoundEnabled(String key) {
        return this.plugin.getConfig().getBoolean("sounds." + key + ".enabled", false);
    }

    public String getSoundName(String key) {
        return this.plugin.getConfig().getString("sounds." + key + ".sound", "");
    }

    public float getSoundVolume(String key) {
        return (float) this.plugin.getConfig().getDouble("sounds." + key + ".volume", 1.0D);
    }

    public float getSoundPitch(String key) {
        return (float) this.plugin.getConfig().getDouble("sounds." + key + ".pitch", 1.0D);
    }
}
