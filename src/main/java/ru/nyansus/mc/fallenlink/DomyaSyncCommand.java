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
            sender.sendMessage(plugin.getMessages().get(sender, "command.reload-success"));
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            plugin.getSyncService().syncOnlinePlayers();
            sender.sendMessage(plugin.getMessages().get(sender, "command.sync-started"));
            return true;
        }

        sender.sendMessage(plugin.getMessages().get(sender, "command.usage"));
        return true;
    }

    private void sendStatus(CommandSender sender) {
        Messages messages = plugin.getMessages();
        sender.sendMessage(messages.get(sender,
                "command.status-title",
                "{version}", plugin.getDescription().getVersion()));
        sender.sendMessage(messages.get(sender,
                "command.status-api",
                "{api}", plugin.getSyncConfig().getApiUrl()));
        sender.sendMessage(messages.get(sender,
                "command.status-online",
                "{count}", String.valueOf(plugin.getServer().getOnlinePlayers().size())));
    }
}
