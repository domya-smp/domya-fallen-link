package ru.nyansus.mc.fallenlink.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.service.PlayerLinkUseCase;
import ru.nyansus.mc.fallenlink.service.SyncAvailability;

public final class LinkCommand implements CommandExecutor {

    private final MessageProvider messages;
    private final SyncAvailability syncAvailability;
    private final SyncConfigProvider configProvider;
    private final PlayerLinkUseCase linkUseCase;

    public LinkCommand(
            MessageProvider messages,
            SyncAvailability syncAvailability,
            SyncConfigProvider configProvider,
            PlayerLinkUseCase linkUseCase
    ) {
        this.messages = messages;
        this.syncAvailability = syncAvailability;
        this.configProvider = configProvider;
        this.linkUseCase = linkUseCase;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get(sender, "command.player-only"));
            return true;
        }

        if (!syncAvailability.isEnabled()) {
            sender.sendMessage(messages.get(sender, "command.sync-paused"));
            return true;
        }

        if (args.length < 1 || args[0].trim().length() < 4) {
            sender.sendMessage(messages.get(sender, "command.link-usage"));
            return true;
        }

        Player player = (Player) sender;
        if (!configProvider.current().hasLinkSettings()) {
            player.sendMessage(messages.get(player, "command.link-not-configured"));
            return true;
        }

        player.sendMessage(messages.get(player, "command.link-checking"));
        linkUseCase.linkPlayer(player, args[0].trim());
        return true;
    }
}
