package ua.xani4ka.xanisethome.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ua.xani4ka.xanisethome.XaniSetHomePlugin;

public final class SetHomeMigrateCommand implements CommandExecutor {
    private final XaniSetHomePlugin plugin;

    public SetHomeMigrateCommand(XaniSetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be used from console.");
            return true;
        }

        boolean imported = this.plugin.migrateLegacyHomes();
        if (imported) {
            sender.sendMessage("Legacy homes imported successfully.");
        } else {
            sender.sendMessage("Legacy homes import failed. Check console for details.");
        }
        return true;
    }
}
