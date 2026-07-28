package ru.nyansus.mc.fallenlink;

import java.time.Clock;
import java.time.ZoneId;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nyansus.mc.fallenlink.api.DomyaApiClient;
import ru.nyansus.mc.fallenlink.api.DomyaPayloadFactory;
import ru.nyansus.mc.fallenlink.command.DomyaSyncCommand;
import ru.nyansus.mc.fallenlink.command.LinkCommand;
import ru.nyansus.mc.fallenlink.config.BukkitSyncConfigProvider;
import ru.nyansus.mc.fallenlink.listener.PlayerSyncListener;
import ru.nyansus.mc.fallenlink.message.Messages;
import ru.nyansus.mc.fallenlink.player.PlayerNameResolver;
import ru.nyansus.mc.fallenlink.player.PlayerPrivacyMapper;
import ru.nyansus.mc.fallenlink.player.PlayerSnapshotFactory;
import ru.nyansus.mc.fallenlink.player.PlayerStatisticsCollector;
import ru.nyansus.mc.fallenlink.player.BukkitStatisticKeyCatalog;
import ru.nyansus.mc.fallenlink.player.StatisticReader;
import ru.nyansus.mc.fallenlink.scheduler.BukkitTaskScheduler;
import ru.nyansus.mc.fallenlink.scheduler.PeriodicSyncScheduler;
import ru.nyansus.mc.fallenlink.serialization.SnapshotJsonSerializer;
import ru.nyansus.mc.fallenlink.service.SyncService;
import ru.nyansus.mc.fallenlink.service.SyncState;

public final class DomyaFallenLink extends JavaPlugin {

    private DomyaApiClient apiClient;
    private Messages messages;
    private BukkitSyncConfigProvider configProvider;
    private SyncService syncService;
    private SyncState syncState;
    private PeriodicSyncScheduler periodicSyncScheduler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createServices();

        BukkitTaskScheduler taskScheduler = new BukkitTaskScheduler(this, getServer());
        getServer().getPluginManager().registerEvents(
                new PlayerSyncListener(configProvider, syncState, syncService, taskScheduler),
                this
        );
        registerCommands();
        periodicSyncScheduler.reschedule();
    }

    @Override
    public void onDisable() {
        if (periodicSyncScheduler != null) {
            periodicSyncScheduler.close();
        }
        if (apiClient != null) {
            apiClient.close();
        }
    }

    private void reloadAll() {
        configProvider.reload();
        syncState.setEnabled(configProvider.current().isSyncEnabled());
        messages.reload();
        periodicSyncScheduler.reschedule();
    }

    private void pauseSynchronization() {
        configProvider.updateSyncEnabled(false);
        syncState.setEnabled(false);
        periodicSyncScheduler.close();
    }

    private void resumeSynchronization() {
        configProvider.updateSyncEnabled(true);
        syncState.setEnabled(true);
        periodicSyncScheduler.reschedule();
    }

    private void createServices() {
        Clock clock = Clock.systemUTC();
        configProvider = new BukkitSyncConfigProvider(this);
        messages = new Messages(this);
        syncState = new SyncState(configProvider.current().isSyncEnabled());

        SnapshotJsonSerializer serializer = new SnapshotJsonSerializer();
        DomyaPayloadFactory payloadFactory = new DomyaPayloadFactory(serializer, clock);
        apiClient = new DomyaApiClient(
                getLogger(),
                messages,
                configProvider,
                payloadFactory,
                getPluginMeta().getVersion()
        );

        PlayerNameResolver nameResolver = new PlayerNameResolver(getServer(), messages, configProvider);
        PlayerPrivacyMapper privacyMapper = new PlayerPrivacyMapper();
        PlayerStatisticsCollector statisticsCollector = new PlayerStatisticsCollector(
                new StatisticReader(),
                privacyMapper,
                new BukkitStatisticKeyCatalog()
        );
        PlayerSnapshotFactory snapshotFactory = new PlayerSnapshotFactory(
                nameResolver,
                configProvider,
                statisticsCollector,
                privacyMapper,
                clock,
                ZoneId.systemDefault()
        );
        BukkitTaskScheduler taskScheduler = new BukkitTaskScheduler(this, getServer());
        syncService = new SyncService(
                getLogger(),
                syncState,
                configProvider,
                messages,
                apiClient,
                getServer()::getOnlinePlayers,
                nameResolver,
                snapshotFactory,
                taskScheduler
        );
        periodicSyncScheduler = new PeriodicSyncScheduler(
                this,
                getServer(),
                syncState,
                configProvider,
                syncService
        );
    }

    private void registerCommands() {
        PluginCommand syncCommand = getCommand("domyasync");
        if (syncCommand != null) {
            syncCommand.setExecutor(new DomyaSyncCommand(
                    messages,
                    syncState,
                    configProvider,
                    syncService,
                    this::reloadAll,
                    this::pauseSynchronization,
                    this::resumeSynchronization,
                    getPluginMeta().getVersion(),
                    () -> getServer().getOnlinePlayers().size()
            ));
        }

        PluginCommand linkCommand = getCommand("link");
        if (linkCommand != null) {
            linkCommand.setExecutor(new LinkCommand(messages, syncState, configProvider, syncService));
        }
    }
}
