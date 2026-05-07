package ua.xani4ka.xanisethome;

import java.util.Arrays;
import org.bukkit.Sound;
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

        Sound sound = Arrays.stream(Sound.values())
            .filter(entry -> entry.name().equalsIgnoreCase(soundName))
            .findFirst()
            .orElse(null);
        if (sound == null) {
            this.plugin.getLogger().warning("Unknown sound '" + soundName + "' for config key '" + basePath + "'.");
            return;
        }

        float volume = this.settings.getSoundVolume(key);
        float pitch = this.settings.getSoundPitch(key);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
