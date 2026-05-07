package ua.xani4ka.xanisethome;

import java.util.Locale;
import org.bukkit.entity.Player;

public final class SoundManager {
    private final XaniSetHomePlugin plugin;
    private final Settings settings;

    public SoundManager(XaniSetHomePlugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void play(Player player, String key) {
        if (!this.settings.isSoundEnabled(key)) {
            return;
        }

        String basePath = "sounds." + key;
        String soundName = this.settings.getSoundName(key);
        if (soundName == null || soundName.isBlank()) {
            return;
        }

        float volume = this.settings.getSoundVolume(key);
        float pitch = this.settings.getSoundPitch(key);
        String resolvedSound = resolveSoundName(soundName);

        try {
            player.playSound(player.getLocation(), resolvedSound, volume, pitch);
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Unknown sound '" + soundName + "' for config key '" + basePath + "'.");
        }
    }

    private String resolveSoundName(String soundName) {
        if (soundName.contains(":")) {
            return soundName.toLowerCase(Locale.ROOT);
        }
        if (soundName.equals(soundName.toUpperCase(Locale.ROOT))) {
            return "minecraft:" + soundName.toLowerCase(Locale.ROOT).replace('_', '.');
        }
        return soundName;
    }
}
