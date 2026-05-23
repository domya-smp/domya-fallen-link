package ru.nyansus.mc.fallenlink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LinkCommand implements CommandExecutor {

    private final DomyaFallenLink plugin;
    private final SyncService syncService;

    public LinkCommand(DomyaFallenLink plugin, SyncService syncService) {
        this.plugin = plugin;
        this.syncService = syncService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду может выполнить только игрок.");
            return true;
        }

        if (args.length < 1 || args[0].trim().length() < 4) {
            sender.sendMessage("§eИспользование: §f/link <код-с-сайта>");
            return true;
        }

        Player player = (Player) sender;
        if (!plugin.getSyncConfig().hasLinkSettings()) {
            player.sendMessage("§cПривязка не настроена. Сообщи администратору.");
            return true;
        }

        player.sendMessage("§7Проверяю код привязки...");
        syncService.linkPlayer(player, args[0].trim());
        return true;
    }
}
