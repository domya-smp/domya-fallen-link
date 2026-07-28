package ru.nyansus.mc.fallenlink.player;

import java.time.Clock;
import java.time.ZoneId;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.config.PrivacyPolicy;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.util.TimeUtil;

public final class PlayerSnapshotFactory implements PlayerSnapshotProvider {

    private final PlayerNameProvider nameProvider;
    private final SyncConfigProvider configProvider;
    private final PlayerStatisticsCollector statisticsCollector;
    private final PlayerPrivacyMapper privacyMapper;
    private final Clock clock;
    private final ZoneId zoneId;

    public PlayerSnapshotFactory(
            PlayerNameProvider nameProvider,
            SyncConfigProvider configProvider,
            PlayerStatisticsCollector statisticsCollector,
            PlayerPrivacyMapper privacyMapper,
            Clock clock,
            ZoneId zoneId
    ) {
        this.nameProvider = nameProvider;
        this.configProvider = configProvider;
        this.statisticsCollector = statisticsCollector;
        this.privacyMapper = privacyMapper;
        this.clock = clock;
        this.zoneId = zoneId;
    }

    @Override
    public PlayerSnapshot create(Player player, boolean online) {
        Location location = player.getLocation();
        World world = player.getWorld();
        SyncConfig config = configProvider.current();
        String publicName = nameProvider.resolve(player);
        PrivacyPolicy privacy = config.getPrivacyPolicy();
        return new PlayerSnapshot(
                player.getUniqueId().toString(),
                publicName,
                publicName,
                online,
                TimeUtil.nowMysql(clock, zoneId),
                TimeUtil.mysqlFromMillis(Math.max(0L, player.getFirstPlayed()), zoneId),
                privacyMapper.worldName(world, privacy),
                privacyMapper.position(location, privacy),
                statisticsCollector.collect(player, privacy)
        );
    }
}
