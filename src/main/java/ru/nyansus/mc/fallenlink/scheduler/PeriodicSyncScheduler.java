package ru.nyansus.mc.fallenlink.scheduler;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.service.PlayerSyncUseCase;
import ru.nyansus.mc.fallenlink.service.SyncAvailability;

public final class PeriodicSyncScheduler implements AutoCloseable {

    private static final long INITIAL_DELAY_TICKS = 80L;
    private static final long MINIMUM_PERIOD_TICKS = 20L;

    private final Plugin plugin;
    private final Server server;
    private final SyncAvailability syncAvailability;
    private final SyncConfigProvider configProvider;
    private final PlayerSyncUseCase syncUseCase;
    private int taskId = -1;

    public PeriodicSyncScheduler(
            Plugin plugin,
            Server server,
            SyncAvailability syncAvailability,
            SyncConfigProvider configProvider,
            PlayerSyncUseCase syncUseCase
    ) {
        this.plugin = plugin;
        this.server = server;
        this.syncAvailability = syncAvailability;
        this.configProvider = configProvider;
        this.syncUseCase = syncUseCase;
    }

    public void reschedule() {
        close();
        if (!syncAvailability.isEnabled()) {
            return;
        }
        long interval = configProvider.current().getSyncIntervalSeconds();
        long periodTicks = Math.max(MINIMUM_PERIOD_TICKS, interval * 20L);
        taskId = server.getScheduler()
                .runTaskTimer(plugin, syncUseCase::syncOnlinePlayers, INITIAL_DELAY_TICKS, periodTicks)
                .getTaskId();
    }

    @Override
    public void close() {
        if (taskId == -1) {
            return;
        }
        server.getScheduler().cancelTask(taskId);
        taskId = -1;
    }
}
