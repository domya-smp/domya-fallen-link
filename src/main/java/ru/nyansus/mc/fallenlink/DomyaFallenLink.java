package ru.nyansus.mc.fallenlink;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nyansus.mc.fallenlink.api.DomyaApiClient;
import ru.nyansus.mc.fallenlink.api.DomyaPayloadFactory;
import ru.nyansus.mc.fallenlink.command.DomyaSyncCommand;
import ru.nyansus.mc.fallenlink.command.LinkCommand;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.listener.PlayerSyncListener;
import ru.nyansus.mc.fallenlink.message.Messages;
import ru.nyansus.mc.fallenlink.player.PlayerNameResolver;
import ru.nyansus.mc.fallenlink.player.PlayerSnapshotFactory;
import ru.nyansus.mc.fallenlink.serialization.SnapshotJsonSerializer;
import ru.nyansus.mc.fallenlink.service.SyncService;

public final class DomyaFallenLink extends JavaPlugin {

    private DomyaApiClient apiClient;
    private Messages messages;
    private SyncConfig syncConfig;
    private SyncService syncService;
    private int periodicTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadServices();

        getServer().getPluginManager().registerEvents(new PlayerSyncListener(this), this);
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

    public Messages getMessages() {
        return messages;
    }

    public void reloadAll() {
        reloadConfig();
        messages.reload();
        reloadServices();
        schedulePeriodicSync();
    }

    private void reloadServices() {
        if (apiClient != null) {
            apiClient.close();
        }
        syncConfig = SyncConfig.from(getConfig());
        if (messages == null) {
            messages = new Messages(this);
        }
        apiClient = new DomyaApiClient(getLogger(), messages, syncConfig, new DomyaPayloadFactory());
        PlayerNameResolver nameResolver = new PlayerNameResolver(getServer(), messages);
        PlayerSnapshotFactory snapshotFactory = new PlayerSnapshotFactory(nameResolver, syncConfig);
        syncService = new SyncService(
                this,
                getServer(),
                getLogger(),
                syncConfig,
                messages,
                apiClient,
                nameResolver,
                snapshotFactory,
                new SnapshotJsonSerializer()
        );
    }

    private void registerCommands() {
        PluginCommand syncCommand = getCommand("domyasync");
        if (syncCommand != null) {
            syncCommand.setExecutor(new DomyaSyncCommand(this));
        }

        PluginCommand linkCommand = getCommand("link");
        if (linkCommand != null) {
            linkCommand.setExecutor(new LinkCommand(this));
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
