package ua.xani4ka.xanisethome;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageManager {
    private final XaniSetHomePlugin plugin;
    private final Settings settings;
    private final MiniMessage miniMessage;
    private YamlConfiguration messages;

    public MessageManager(XaniSetHomePlugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.miniMessage = MiniMessage.miniMessage();
        reload();
    }

    public void reload() {
        String language = this.settings.getLanguage();
        String resourcePath = "messages/" + language + ".yml";
        File file = new File(this.plugin.getDataFolder(), resourcePath);

        if (!file.exists()) {
            this.plugin.saveResource("messages/en.yml", false);
            this.plugin.getLogger().warning("Language '" + language + "' was not found. Falling back to 'en'.");
            resourcePath = "messages/en.yml";
            file = new File(this.plugin.getDataFolder(), resourcePath);
        }

        this.messages = YamlConfiguration.loadConfiguration(file);
        try (InputStream stream = this.plugin.getResource(resourcePath)) {
            if (stream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
                this.messages.setDefaults(defaults);
                this.messages.options().copyDefaults(true);
            }
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Could not load default messages for '" + resourcePath + "': " + exception.getMessage());
        }

        ConfigurationSection overrides = this.settings.getMessageOverrides();
        if (overrides != null) {
            for (String key : overrides.getKeys(true)) {
                if (overrides.isConfigurationSection(key)) {
                    continue;
                }
                this.messages.set(key, overrides.get(key));
            }
        }
    }

    public Component get(String key, String... replacements) {
        String raw = this.messages.getString(key);
        if (raw == null) {
            raw = "<red>Missing message key: " + key + "</red>";
        }

        for (int index = 0; index + 1 < replacements.length; index += 2) {
            raw = raw.replace("{" + replacements[index] + "}", replacements[index + 1]);
        }
        return this.miniMessage.deserialize(raw);
    }
}
