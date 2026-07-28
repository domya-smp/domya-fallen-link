package ru.nyansus.mc.fallenlink.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.api.ApiResponse;
import ru.nyansus.mc.fallenlink.api.DomyaGateway;
import ru.nyansus.mc.fallenlink.api.LinkResult;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.model.PlayerStats;
import ru.nyansus.mc.fallenlink.model.Position;
import ru.nyansus.mc.fallenlink.support.TestPlayers;

public final class SyncServiceTest {

    @Test
    public void syncOnlinePlayersBuildsSnapshotsAndSendsOneBatch() {
        Player first = TestPlayers.player("first", TestPlayers.messages());
        Player second = TestPlayers.player("second", TestPlayers.messages());
        FakeGateway gateway = new FakeGateway();
        AtomicInteger snapshotsCreated = new AtomicInteger();
        SyncService service = service(
                List.of(first, second),
                gateway,
                player -> {
                    snapshotsCreated.incrementAndGet();
                    return snapshot(player.getName());
                },
                new AtomicInteger()
        );

        service.syncOnlinePlayers();

        Assert.assertEquals(2, snapshotsCreated.get());
        Assert.assertEquals(1, gateway.syncBatches.size());
        Assert.assertEquals(2, gateway.syncBatches.get(0).size());
    }

    @Test
    public void emptyAndNullSyncDoNotCallGateway() {
        FakeGateway gateway = new FakeGateway();
        SyncService service = service(List.of(), gateway, player -> snapshot("unused"), new AtomicInteger());

        service.syncOnlinePlayers();
        service.syncPlayer(null, true);

        Assert.assertTrue(gateway.syncBatches.isEmpty());
    }

    @Test
    public void pausedStateBlocksEveryDataExchangePath() {
        Player player = TestPlayers.player("paused", TestPlayers.messages());
        FakeGateway gateway = new FakeGateway();
        AtomicInteger snapshotsCreated = new AtomicInteger();
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        SyncService service = new SyncService(
                Logger.getLogger("test"),
                () -> false,
                () -> config,
                new KeyMessages(),
                gateway,
                () -> List.of(player),
                ignored -> "public-name",
                (ignored, online) -> {
                    snapshotsCreated.incrementAndGet();
                    return snapshot("paused");
                },
                Runnable::run
        );

        service.syncOnlinePlayers();
        service.syncPlayer(player, true);
        service.linkPlayer(player, "code");

        Assert.assertEquals(0, snapshotsCreated.get());
        Assert.assertTrue(gateway.syncBatches.isEmpty());
        Assert.assertNull(gateway.linkRequest);
    }

    @Test
    public void successfulLinkReturnsToMainThreadAndSyncsPlayer() {
        List<String> messages = TestPlayers.messages();
        Player player = TestPlayers.player("linked", messages);
        FakeGateway gateway = new FakeGateway();
        gateway.linkResult = LinkResult.from(new ApiResponse(200, "{\"ok\":true}"));
        AtomicInteger mainThreadExecutions = new AtomicInteger();
        SyncService service = service(
                List.of(),
                gateway,
                ignored -> snapshot("linked"),
                mainThreadExecutions
        );

        service.linkPlayer(player, "code");

        Assert.assertEquals("code", gateway.linkRequest.getCode());
        Assert.assertEquals("public-name", gateway.linkRequest.getNickname());
        Assert.assertEquals(1, mainThreadExecutions.get());
        Assert.assertEquals(List.of("command.link-success"), messages);
        Assert.assertEquals(1, gateway.syncBatches.size());
    }

    @Test
    public void linkFailuresProduceSpecificMessagesWithoutSync() {
        assertLinkFailure(
                LinkResult.from(new ApiResponse(0, "")),
                "command.link-connection-error"
        );
        assertLinkFailure(
                LinkResult.from(new ApiResponse(422, "bad code")),
                "command.link-failed"
        );
        assertLinkFailure(
                LinkResult.notConfigured(),
                "command.link-not-configured"
        );
    }

    private void assertLinkFailure(LinkResult result, String expectedMessage) {
        List<String> messages = TestPlayers.messages();
        Player player = TestPlayers.player("failed", messages);
        FakeGateway gateway = new FakeGateway();
        gateway.linkResult = result;
        SyncService service = service(List.of(), gateway, ignored -> snapshot("failed"), new AtomicInteger());

        service.linkPlayer(player, "code");

        Assert.assertEquals(List.of(expectedMessage), messages);
        Assert.assertTrue(gateway.syncBatches.isEmpty());
    }

    private SyncService service(
            Collection<? extends Player> onlinePlayers,
            FakeGateway gateway,
            SnapshotCreator snapshotCreator,
            AtomicInteger mainThreadExecutions
    ) {
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        return new SyncService(
                Logger.getLogger("test"),
                () -> true,
                () -> config,
                new KeyMessages(),
                gateway,
                () -> onlinePlayers,
                player -> "public-name",
                (player, online) -> snapshotCreator.create(player),
                task -> {
                    mainThreadExecutions.incrementAndGet();
                    task.run();
                }
        );
    }

    private static PlayerSnapshot snapshot(String name) {
        PlayerStats stats = new PlayerStats(
                0, 0, 0, 0, Map.of(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 20);
        return new PlayerSnapshot(
                name, name, name, true, "last", "first", "world", new Position(0, 0, 0), stats);
    }

    private interface SnapshotCreator {

        PlayerSnapshot create(Player player);
    }

    private static final class KeyMessages implements MessageProvider {

        @Override
        public String get(CommandSender sender, String key, String... replacements) {
            return key;
        }

        @Override
        public String get(String key, String... replacements) {
            return key;
        }
    }

    private static final class FakeGateway implements DomyaGateway {

        private final List<List<PlayerSnapshot>> syncBatches = new ArrayList<>();
        private LinkResult linkResult = LinkResult.notConfigured();
        private PlayerLinkRequest linkRequest;

        @Override
        public CompletableFuture<ApiResponse> syncPlayers(Collection<PlayerSnapshot> players) {
            syncBatches.add(List.copyOf(players));
            return CompletableFuture.completedFuture(new ApiResponse(200, "{}"));
        }

        @Override
        public CompletableFuture<LinkResult> linkPlayer(PlayerLinkRequest request) {
            linkRequest = request;
            return CompletableFuture.completedFuture(linkResult);
        }

        @Override
        public void close() {
        }
    }
}
