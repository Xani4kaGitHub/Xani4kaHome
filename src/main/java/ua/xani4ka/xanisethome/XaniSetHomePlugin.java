package ua.xani4ka.xanisethome;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ua.xani4ka.xanisethome.command.AdminDelHomeCommand;
import ua.xani4ka.xanisethome.command.AdminListHomesCommand;
import ua.xani4ka.xanisethome.command.DelHomeCommand;
import ua.xani4ka.xanisethome.command.HomeCommand;
import ua.xani4ka.xanisethome.command.SetHomeCommand;
import ua.xani4ka.xanisethome.command.SetHomeMigrateCommand;

public final class XaniSetHomePlugin extends JavaPlugin {
    private Settings settings;
    private MessageManager messageManager;
    private HomeService homeService;
    private SoundManager soundManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveBundledMessageFile("messages/en.yml");
        saveBundledMessageFile("messages/ru.yml");
        saveBundledMessageFile("messages/uk.yml");
        saveBundledMessageFile("messages/pl.yml");
        saveBundledMessageFile("messages/de.yml");
        saveBundledMessageFile("messages/fr.yml");
        saveBundledMessageFile("messages/nl.yml");

        this.settings = new Settings(this);
        this.messageManager = new MessageManager(this, this.settings);
        this.soundManager = new SoundManager(this, this.settings);
        this.homeService = new HomeService(this, this.settings, new HomeStorage(this, this.settings));

        registerCommand("sethome", new SetHomeCommand(this.homeService, this.messageManager, this.soundManager));
        registerCommand("home", new HomeCommand(this, this.homeService, this.messageManager, this.soundManager));
        registerCommand("delhome", new DelHomeCommand(this.homeService, this.messageManager, this.soundManager));
        registerCommand("admindelhome", new AdminDelHomeCommand(this.homeService, this.messageManager));
        registerCommand("adminlisthomes", new AdminListHomesCommand(this.homeService, this.messageManager));
        registerCommand("sethomemigrate", new SetHomeMigrateCommand(this));

        HomeTabCompleter completer = new HomeTabCompleter(this.homeService);
        getCommand("home").setTabCompleter(completer);
        getCommand("delhome").setTabCompleter(completer);
    }

    @Override
    public void onDisable() {
        if (this.homeService != null) {
            this.homeService.saveHomes();
        }
    }

    private void saveBundledMessageFile(String path) {
        File target = new File(getDataFolder(), path);
        if (!target.exists()) {
            saveResource(path, false);
        }
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is missing from plugin.yml.");
            return;
        }
        command.setExecutor(executor);
    }

    public boolean migrateLegacyHomes() {
        File targetHomes = new File(getDataFolder(), "homes.yml");
        String folderName = this.settings.getLegacyImportFolder();
        String fileName = this.settings.getLegacyImportFile();
        File legacyFolder = new File(getDataFolder(), folderName);
        File legacyHomes = new File(legacyFolder, fileName);
        if (!legacyHomes.isFile()) {
            getLogger().warning("Legacy homes file was not found at '" + folderName + "/" + fileName + "'.");
            return false;
        }

        try {
            Files.createDirectories(getDataFolder().toPath());
            Files.copy(legacyHomes.toPath(), targetHomes.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.homeService.reloadHomes();
            getLogger().info("Imported legacy homes from '" + folderName + "/" + fileName + "'.");
            return true;
        } catch (IOException exception) {
            getLogger().severe("Could not import legacy homes from '" + folderName + "/" + fileName + "': " + exception.getMessage());
            return false;
        }
    }
}
