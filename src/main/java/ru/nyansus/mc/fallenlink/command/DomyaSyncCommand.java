package ru.nyansus.mc.fallenlink.command;

import java.util.function.IntSupplier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.service.PlayerSyncUseCase;
import ru.nyansus.mc.fallenlink.service.SyncAvailability;

public final class DomyaSyncCommand implements CommandExecutor {

    private final MessageProvider messages;
    private final SyncAvailability syncAvailability;
    private final SyncConfigProvider configProvider;
    private final PlayerSyncUseCase syncUseCase;
    private final Runnable reloadAction;
    private final Runnable pauseAction;
    private final Runnable resumeAction;
    private final String version;
    private final IntSupplier onlinePlayerCount;

    public DomyaSyncCommand(
            MessageProvider messages,
            SyncAvailability syncAvailability,
            SyncConfigProvider configProvider,
            PlayerSyncUseCase syncUseCase,
            Runnable reloadAction,
            Runnable pauseAction,
            Runnable resumeAction,
            String version,
            IntSupplier onlinePlayerCount
    ) {
        this.messages = messages;
        this.syncAvailability = syncAvailability;
        this.configProvider = configProvider;
        this.syncUseCase = syncUseCase;
        this.reloadAction = reloadAction;
        this.pauseAction = pauseAction;
        this.resumeAction = resumeAction;
        this.version = version;
        this.onlinePlayerCount = onlinePlayerCount;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sendStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadAction.run();
            sender.sendMessage(messages.get(sender, "command.reload-success"));
            return true;
        }

        if (args[0].equalsIgnoreCase("pause")) {
            pause(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("resume")) {
            resume(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            if (!syncAvailability.isEnabled()) {
                sender.sendMessage(messages.get(sender, "command.sync-paused"));
                return true;
            }
            syncUseCase.syncOnlinePlayers();
            sender.sendMessage(messages.get(sender, "command.sync-started"));
            return true;
        }

        sender.sendMessage(messages.get(sender, "command.usage"));
        return true;
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage(messages.get(sender,
                "command.status-title",
                "{version}", version));
        sender.sendMessage(messages.get(sender,
                "command.status-api",
                "{api}", configProvider.current().getApiUrl()));
        sender.sendMessage(messages.get(sender,
                "command.status-online",
                "{count}", String.valueOf(onlinePlayerCount.getAsInt())));
        sender.sendMessage(messages.get(sender,
                "command.status-state",
                "{state}", messages.get(sender, syncAvailability.isEnabled()
                        ? "command.state-running"
                        : "command.state-paused")));
    }

    private void pause(CommandSender sender) {
        if (!syncAvailability.isEnabled()) {
            sender.sendMessage(messages.get(sender, "command.pause-already"));
            return;
        }
        pauseAction.run();
        sender.sendMessage(messages.get(sender, "command.pause-success"));
    }

    private void resume(CommandSender sender) {
        if (syncAvailability.isEnabled()) {
            sender.sendMessage(messages.get(sender, "command.resume-already"));
            return;
        }
        resumeAction.run();
        sender.sendMessage(messages.get(sender, "command.resume-success"));
    }
}
