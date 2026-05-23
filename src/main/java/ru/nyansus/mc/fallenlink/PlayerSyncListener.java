package ru.nyansus.mc.fallenlink;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerSyncListener implements Listener {

    private final DomyaFallenLink plugin;
    private final SyncService syncService;

    public PlayerSyncListener(DomyaFallenLink plugin, SyncService syncService) {
        this.plugin = plugin;
        this.syncService = syncService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getSyncConfig().isSyncOnJoin()) {
            plugin.getServer().getScheduler()
                    .runTaskLater(plugin, () -> syncService.syncPlayer(event.getPlayer(), true), 40L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getSyncConfig().isSyncOnQuit()) {
            syncService.syncPlayer(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        plugin.getServer().getScheduler()
                .runTaskLater(plugin, () -> syncService.syncPlayer(event.getEntity(), true), 40L);
    }
}
