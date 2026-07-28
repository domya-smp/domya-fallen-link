package ru.nyansus.mc.fallenlink.listener;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.scheduler.DelayedTaskScheduler;
import ru.nyansus.mc.fallenlink.service.PlayerSyncUseCase;
import ru.nyansus.mc.fallenlink.support.TestPlayers;

public final class PlayerSyncListenerTest {

    @Test
    public void routesEnabledPlayerEventsThroughUseCaseAndScheduler() {
        Player player = TestPlayers.player("Steve", TestPlayers.messages());
        RecordingSyncUseCase syncUseCase = new RecordingSyncUseCase();
        RecordingScheduler scheduler = new RecordingScheduler();
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        PlayerSyncListener listener = new PlayerSyncListener(() -> config, () -> true, syncUseCase, scheduler);

        listener.onJoin(new PlayerJoinEvent(player, Component.empty()));
        listener.onQuit(new PlayerQuitEvent(player, Component.empty()));
        listener.onDeath(new PlayerDeathEvent(player, null, List.<ItemStack>of(), 0, ""));

        Assert.assertEquals(List.of(40L, 40L), scheduler.delays);
        Assert.assertEquals(List.of(false), syncUseCase.onlineStates);

        scheduler.tasks.forEach(Runnable::run);
        Assert.assertEquals(List.of(false, true, true), syncUseCase.onlineStates);
    }

    @Test
    public void disabledJoinAndQuitOptionsSuppressTheirSyncs() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("sync-on-join", false);
        yaml.set("sync-on-quit", false);
        SyncConfig config = SyncConfig.from(yaml);
        RecordingSyncUseCase syncUseCase = new RecordingSyncUseCase();
        RecordingScheduler scheduler = new RecordingScheduler();
        Player player = TestPlayers.player("Steve", TestPlayers.messages());
        PlayerSyncListener listener = new PlayerSyncListener(() -> config, () -> true, syncUseCase, scheduler);

        listener.onJoin(new PlayerJoinEvent(player, Component.empty()));
        listener.onQuit(new PlayerQuitEvent(player, Component.empty()));

        Assert.assertTrue(scheduler.tasks.isEmpty());
        Assert.assertTrue(syncUseCase.onlineStates.isEmpty());
    }

    @Test
    public void pausedStateSuppressesAllPlayerEvents() {
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        RecordingSyncUseCase syncUseCase = new RecordingSyncUseCase();
        RecordingScheduler scheduler = new RecordingScheduler();
        Player player = TestPlayers.player("Steve", TestPlayers.messages());
        PlayerSyncListener listener = new PlayerSyncListener(() -> config, () -> false, syncUseCase, scheduler);

        listener.onJoin(new PlayerJoinEvent(player, Component.empty()));
        listener.onQuit(new PlayerQuitEvent(player, Component.empty()));
        listener.onDeath(new PlayerDeathEvent(player, null, List.<ItemStack>of(), 0, ""));

        Assert.assertTrue(scheduler.tasks.isEmpty());
        Assert.assertTrue(syncUseCase.onlineStates.isEmpty());
    }

    private static final class RecordingScheduler implements DelayedTaskScheduler {

        private final List<Runnable> tasks = new ArrayList<>();
        private final List<Long> delays = new ArrayList<>();

        @Override
        public void runLater(Runnable task, long delayTicks) {
            tasks.add(task);
            delays.add(delayTicks);
        }
    }

    private static final class RecordingSyncUseCase implements PlayerSyncUseCase {

        private final List<Boolean> onlineStates = new ArrayList<>();

        @Override
        public void syncOnlinePlayers() {
        }

        @Override
        public void syncPlayer(Player player, boolean online) {
            onlineStates.add(online);
        }
    }
}
