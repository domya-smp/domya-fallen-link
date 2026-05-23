package ru.nyansus.mc.fallenlink.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.nyansus.mc.fallenlink.DomyaFallenLink;

public final class PlayerSyncListener implements Listener {

    private final DomyaFallenLink plugin;

    public PlayerSyncListener(DomyaFallenLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getSyncConfig().isSyncOnJoin()) {
            plugin.getServer().getScheduler()
                    .runTaskLater(plugin, () -> plugin.getSyncService().syncPlayer(event.getPlayer(), true), 40L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getSyncConfig().isSyncOnQuit()) {
            plugin.getSyncService().syncPlayer(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        plugin.getServer().getScheduler()
                .runTaskLater(plugin, () -> plugin.getSyncService().syncPlayer(event.getEntity(), true), 40L);
    }
}
