package ru.nyansus.mc.fallenlink;

import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

public final class SyncService {

    private final DomyaFallenLink plugin;
    private final DomyaApiClient apiClient;
    private final PlayerSnapshotFactory snapshotFactory;

    public SyncService(DomyaFallenLink plugin, DomyaApiClient apiClient, PlayerSnapshotFactory snapshotFactory) {
        this.plugin = plugin;
        this.apiClient = apiClient;
        this.snapshotFactory = snapshotFactory;
    }

    public void syncOnlinePlayers() {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        if (players.isEmpty()) {
            if (plugin.getSyncConfig().isDebug()) {
                plugin.getLogger().info("No online players to sync.");
            }
            return;
        }

        String playersJson = players.stream()
                .map(player -> snapshotFactory.create(player, true).toJson())
                .collect(Collectors.joining(",", "[", "]"));
        sendPlayers(playersJson);
    }

    public void syncPlayer(Player player, boolean online) {
        if (player == null) {
            return;
        }
        String playersJson = "[" + snapshotFactory.create(player, online).toJson() + "]";
        sendPlayers(playersJson);
    }

    public void linkPlayer(Player player, String code) {
        PlayerLinkRequest request = PlayerLinkRequest.from(player, code);
        apiClient.sendLinkRequest(request).thenAccept(response -> plugin.getServer().getScheduler()
                .runTask(plugin, () -> handleLinkResponse(player, response)));
    }

    private void handleLinkResponse(Player player, ApiResponse response) {
        if (response.getStatusCode() == 0) {
            player.sendMessage("§cОшибка связи с сайтом. Попробуй позже.");
            return;
        }

        if (response.isOkJson()) {
            player.sendMessage("§aПрофиль успешно привязан к сайту Domya SMP.");
            syncPlayer(player, true);
            return;
        }

        player.sendMessage("§cНе удалось привязать профиль. Проверь код на сайте.");
        if (plugin.getSyncConfig().isDebug()) {
            plugin.getLogger().warning("Link failed: HTTP " + response.getStatusCode() + " " + response.getBody());
        }
    }

    private void sendPlayers(String playersJson) {
        apiClient.sendSyncPayload(playersJson);
    }
}
