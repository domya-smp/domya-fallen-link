package ru.nyansus.mc.fallenlink.service;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.api.DomyaGateway;
import ru.nyansus.mc.fallenlink.api.LinkResult;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.player.OnlinePlayerProvider;
import ru.nyansus.mc.fallenlink.player.PlayerNameProvider;
import ru.nyansus.mc.fallenlink.player.PlayerSnapshotProvider;

public final class SyncService implements PlayerSyncUseCase, PlayerLinkUseCase {

    private final Logger logger;
    private final SyncAvailability syncAvailability;
    private final SyncConfigProvider configProvider;
    private final MessageProvider messages;
    private final DomyaGateway gateway;
    private final OnlinePlayerProvider onlinePlayerProvider;
    private final PlayerNameProvider nameProvider;
    private final PlayerSnapshotProvider snapshotProvider;
    private final MainThreadExecutor mainThreadExecutor;

    public SyncService(
            Logger logger,
            SyncAvailability syncAvailability,
            SyncConfigProvider configProvider,
            MessageProvider messages,
            DomyaGateway gateway,
            OnlinePlayerProvider onlinePlayerProvider,
            PlayerNameProvider nameProvider,
            PlayerSnapshotProvider snapshotProvider,
            MainThreadExecutor mainThreadExecutor
    ) {
        this.logger = logger;
        this.syncAvailability = syncAvailability;
        this.configProvider = configProvider;
        this.messages = messages;
        this.gateway = gateway;
        this.onlinePlayerProvider = onlinePlayerProvider;
        this.nameProvider = nameProvider;
        this.snapshotProvider = snapshotProvider;
        this.mainThreadExecutor = mainThreadExecutor;
    }

    @Override
    public void syncOnlinePlayers() {
        if (!syncAvailability.isEnabled()) {
            return;
        }
        Collection<? extends Player> players = onlinePlayerProvider.getOnlinePlayers();
        if (players.isEmpty()) {
            if (configProvider.current().isDebug()) {
                logger.info(messages.get("log.no-online-players"));
            }
            return;
        }

        List<PlayerSnapshot> snapshots = players.stream()
                .map(player -> snapshotProvider.create(player, true))
                .toList();
        gateway.syncPlayers(snapshots);
    }

    @Override
    public void syncPlayer(Player player, boolean online) {
        if (!syncAvailability.isEnabled() || player == null) {
            return;
        }
        gateway.syncPlayers(List.of(snapshotProvider.create(player, online)));
    }

    @Override
    public void linkPlayer(Player player, String code) {
        if (!syncAvailability.isEnabled()) {
            return;
        }
        String publicName = nameProvider.resolve(player);
        PlayerLinkRequest request = new PlayerLinkRequest(
                code,
                player.getUniqueId().toString(),
                publicName,
                publicName
        );
        gateway.linkPlayer(request)
                .thenAccept(result -> mainThreadExecutor.execute(() -> {
                    if (syncAvailability.isEnabled()) {
                        handleLinkResult(player, result);
                    }
                }));
    }

    private void handleLinkResult(Player player, LinkResult result) {
        switch (result.getStatus()) {
            case SUCCESS:
                player.sendMessage(messages.get(player, "command.link-success"));
                syncPlayer(player, true);
                break;
            case CONNECTION_ERROR:
                player.sendMessage(messages.get(player, "command.link-connection-error"));
                break;
            case NOT_CONFIGURED:
                player.sendMessage(messages.get(player, "command.link-not-configured"));
                break;
            case HTTP_ERROR:
                handleLinkHttpError(player, result);
                break;
            default:
                throw new IllegalStateException("Unknown link status: " + result.getStatus());
        }
    }

    private void handleLinkHttpError(Player player, LinkResult result) {
        player.sendMessage(messages.get(player, "command.link-failed"));
        SyncConfig config = configProvider.current();
        if (config.isDebug()) {
            logger.warning(messages.get("log.link-failed",
                    "{status}", String.valueOf(result.getStatusCode()),
                    "{body}", result.getBody()));
        }
    }
}
