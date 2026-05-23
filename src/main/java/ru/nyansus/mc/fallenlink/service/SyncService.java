package ru.nyansus.mc.fallenlink.service;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.nyansus.mc.fallenlink.api.ApiResponse;
import ru.nyansus.mc.fallenlink.api.DomyaApiClient;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.message.Messages;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.player.PlayerNameResolver;
import ru.nyansus.mc.fallenlink.player.PlayerSnapshotFactory;
import ru.nyansus.mc.fallenlink.serialization.SnapshotJsonSerializer;

public final class SyncService {

    private final Plugin plugin;
    private final Server server;
    private final Logger logger;
    private final SyncConfig config;
    private final Messages messages;
    private final DomyaApiClient apiClient;
    private final PlayerNameResolver nameResolver;
    private final PlayerSnapshotFactory snapshotFactory;
    private final SnapshotJsonSerializer snapshotJsonSerializer;

    public SyncService(
            Plugin plugin,
            Server server,
            Logger logger,
            SyncConfig config,
            Messages messages,
            DomyaApiClient apiClient,
            PlayerNameResolver nameResolver,
            PlayerSnapshotFactory snapshotFactory,
            SnapshotJsonSerializer snapshotJsonSerializer
    ) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.config = config;
        this.messages = messages;
        this.apiClient = apiClient;
        this.nameResolver = nameResolver;
        this.snapshotFactory = snapshotFactory;
        this.snapshotJsonSerializer = snapshotJsonSerializer;
    }

    public void syncOnlinePlayers() {
        Collection<? extends Player> players = server.getOnlinePlayers();
        if (players.isEmpty()) {
            if (config.isDebug()) {
                logger.info(messages.get("log.no-online-players"));
            }
            return;
        }

        String playersJson = players.stream()
                .map(player -> snapshotJsonSerializer.serialize(snapshotFactory.create(player, true)))
                .collect(Collectors.joining(",", "[", "]"));
        sendPlayers(playersJson);
    }

    public void syncPlayer(Player player, boolean online) {
        if (player == null) {
            return;
        }
        String playersJson = "[" + snapshotJsonSerializer.serialize(snapshotFactory.create(player, online)) + "]";
        sendPlayers(playersJson);
    }

    public void linkPlayer(Player player, String code) {
        PlayerLinkRequest request = PlayerLinkRequest.from(player, code, nameResolver.resolve(player, config));
        apiClient.sendLinkRequest(request).thenAccept(response -> server.getScheduler()
                .runTask(plugin, () -> handleLinkResponse(player, response)));
    }

    private void handleLinkResponse(Player player, ApiResponse response) {
        if (response.getStatusCode() == 0) {
            player.sendMessage(messages.get(player, "command.link-connection-error"));
            return;
        }

        if (response.isOkJson()) {
            player.sendMessage(messages.get(player, "command.link-success"));
            syncPlayer(player, true);
            return;
        }

        player.sendMessage(messages.get(player, "command.link-failed"));
        if (config.isDebug()) {
            logger.warning(messages.get("log.link-failed",
                    "{status}", String.valueOf(response.getStatusCode()),
                    "{body}", response.getBody()));
        }
    }

    private void sendPlayers(String playersJson) {
        apiClient.sendSyncPayload(playersJson);
    }
}
