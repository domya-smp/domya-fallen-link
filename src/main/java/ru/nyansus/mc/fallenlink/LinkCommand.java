package ru.nyansus.mc.fallenlink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LinkCommand implements CommandExecutor {

    private final DomyaFallenLink plugin;

    public LinkCommand(DomyaFallenLink plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = plugin.getMessages();
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get(sender, "command.player-only"));
            return true;
        }

        if (args.length < 1 || args[0].trim().length() < 4) {
            sender.sendMessage(messages.get(sender, "command.link-usage"));
            return true;
        }

        Player player = (Player) sender;
        if (!plugin.getSyncConfig().hasLinkSettings()) {
            player.sendMessage(messages.get(player, "command.link-not-configured"));
            return true;
        }

        player.sendMessage(messages.get(player, "command.link-checking"));
        plugin.getSyncService().linkPlayer(player, args[0].trim());
        return true;
    }
}
