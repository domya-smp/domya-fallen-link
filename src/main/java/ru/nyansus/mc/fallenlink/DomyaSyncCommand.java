package ru.nyansus.mc.fallenlink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class DomyaSyncCommand implements CommandExecutor {

    private final DomyaFallenLink plugin;

    public DomyaSyncCommand(DomyaFallenLink plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sendStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            sender.sendMessage("§aDomyaPlayerSync config reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            plugin.getSyncService().syncOnlinePlayers();
            sender.sendMessage("§aSync started.");
            return true;
        }

        sender.sendMessage("§cUsage: /domyasync <sync|reload|status> or /link <code>");
        return true;
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage("§bDomyaPlayerSync §7v" + plugin.getDescription().getVersion());
        sender.sendMessage("§7API: §f" + plugin.getSyncConfig().getApiUrl());
        sender.sendMessage("§7Online players: §f" + plugin.getServer().getOnlinePlayers().size());
    }
}
