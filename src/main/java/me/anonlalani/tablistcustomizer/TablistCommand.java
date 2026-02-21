package me.anonlalani.tablistcustomizer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TablistCommand implements CommandExecutor {

    private final TablistCustomizer plugin;

    public TablistCommand(TablistCustomizer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tablistcustomizer.reload")) {
            sender.sendMessage("§cDafür hast du keine Rechte.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage("§aTablistCustomizer neu geladen.");
            return true;
        }

        sender.sendMessage("§7Verwendung: §f/" + label + " reload");
        return true;
    }
}
