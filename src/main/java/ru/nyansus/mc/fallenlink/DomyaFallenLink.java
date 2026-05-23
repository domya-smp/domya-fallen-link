package ru.nyansus.mc.fallenlink;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DomyaFallenLink extends JavaPlugin {

    private DomyaApiClient apiClient;
    private SyncConfig syncConfig;
    private SyncService syncService;
    private int periodicTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadServices();

        getServer().getPluginManager().registerEvents(new PlayerSyncListener(this, syncService), this);
        registerCommands();
        schedulePeriodicSync();
    }

    @Override
    public void onDisable() {
        cancelPeriodicSync();
        if (apiClient != null) {
            apiClient.close();
        }
    }

    public SyncConfig getSyncConfig() {
        return syncConfig;
    }

    public SyncService getSyncService() {
        return syncService;
    }

    public void reloadAll() {
        reloadConfig();
        reloadServices();
        schedulePeriodicSync();
    }

    private void reloadServices() {
        if (apiClient != null) {
            apiClient.close();
        }
        syncConfig = SyncConfig.from(getConfig());
        apiClient = new DomyaApiClient(getLogger(), syncConfig);
        PlayerSnapshotFactory snapshotFactory = new PlayerSnapshotFactory();
        syncService = new SyncService(this, apiClient, snapshotFactory);
    }

    private void registerCommands() {
        PluginCommand syncCommand = getCommand("domyasync");
        if (syncCommand != null) {
            syncCommand.setExecutor(new DomyaSyncCommand(this));
        }

        PluginCommand linkCommand = getCommand("link");
        if (linkCommand != null) {
            linkCommand.setExecutor(new LinkCommand(this, syncService));
        }
    }

    private void schedulePeriodicSync() {
        cancelPeriodicSync();
        long periodTicks = Math.max(20L, syncConfig.getSyncIntervalSeconds() * 20L);
        periodicTaskId = getServer().getScheduler()
                .runTaskTimer(this, syncService::syncOnlinePlayers, 80L, periodTicks)
                .getTaskId();
    }

    private void cancelPeriodicSync() {
        if (periodicTaskId != -1) {
            getServer().getScheduler().cancelTask(periodicTaskId);
            periodicTaskId = -1;
        }
    }
}
