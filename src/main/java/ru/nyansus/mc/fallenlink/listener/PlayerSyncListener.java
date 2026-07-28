package ru.nyansus.mc.fallenlink.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.scheduler.DelayedTaskScheduler;
import ru.nyansus.mc.fallenlink.service.PlayerSyncUseCase;
import ru.nyansus.mc.fallenlink.service.SyncAvailability;

public final class PlayerSyncListener implements Listener {

    private final SyncConfigProvider configProvider;
    private final SyncAvailability syncAvailability;
    private final PlayerSyncUseCase syncUseCase;
    private final DelayedTaskScheduler taskScheduler;

    public PlayerSyncListener(
            SyncConfigProvider configProvider,
            SyncAvailability syncAvailability,
            PlayerSyncUseCase syncUseCase,
            DelayedTaskScheduler taskScheduler
    ) {
        this.configProvider = configProvider;
        this.syncAvailability = syncAvailability;
        this.syncUseCase = syncUseCase;
        this.taskScheduler = taskScheduler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (syncAvailability.isEnabled() && configProvider.current().isSyncOnJoin()) {
            taskScheduler.runLater(() -> syncUseCase.syncPlayer(event.getPlayer(), true), 40L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (syncAvailability.isEnabled() && configProvider.current().isSyncOnQuit()) {
            syncUseCase.syncPlayer(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (syncAvailability.isEnabled()) {
            taskScheduler.runLater(() -> syncUseCase.syncPlayer(event.getEntity(), true), 40L);
        }
    }
}
