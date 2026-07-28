package ru.nyansus.mc.fallenlink.player;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import be.seeseemelk.mockbukkit.MockBukkit;
import ru.nyansus.mc.fallenlink.config.PrivacyPolicy;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.model.PlayerStats;
import ru.nyansus.mc.fallenlink.support.TestPlayers;

public final class PlayerSnapshotFactoryTest {

    @Before
    public void setUpBukkit() {
        MockBukkit.mock();
    }

    @After
    public void tearDownBukkit() {
        MockBukkit.unmock();
    }

    @Test
    public void createsDeterministicSnapshotFromSeparatedComponents() {
        Map<String, Integer> statistics = Map.of(
                "PLAY_ONE_MINUTE", 40,
                "DEATHS", 2,
                "PLAYER_KILLS", 3,
                "MOB_KILLS", 4,
                "KILL_ENTITY:ZOMBIE", 5,
                "JUMP", 6,
                "WALK_ONE_CM", 7,
                "MINE_BLOCK:STONE", 8
        );
        Player player = TestPlayers.player("Steve", TestPlayers.messages(), statistics);
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        PlayerPrivacyMapper privacyMapper = new PlayerPrivacyMapper();
        StatisticKeyCatalog catalog = new StatisticKeyCatalog() {
            @Override
            public List<Material> blockMaterials() {
                return List.of(Material.STONE);
            }

            @Override
            public List<EntityType> mobTypes() {
                return List.of(EntityType.ZOMBIE);
            }
        };
        PlayerStatisticsCollector collector = new PlayerStatisticsCollector(
                new StatisticReader(),
                privacyMapper,
                catalog
        );
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T10:15:30Z"), ZoneOffset.UTC);
        PlayerSnapshotFactory factory = new PlayerSnapshotFactory(
                ignored -> "Public Steve",
                () -> config,
                collector,
                privacyMapper,
                clock,
                ZoneId.of("Asia/Omsk")
        );

        PlayerSnapshot snapshot = factory.create(player, true);

        Assert.assertEquals("Public Steve", snapshot.getNickname());
        Assert.assertEquals("2026-07-28 16:15:30", snapshot.getLastSeenAt());
        Assert.assertEquals("1970-01-01 06:00:00", snapshot.getFirstSeenAt());
        Assert.assertEquals("world", snapshot.getWorld());
        Assert.assertEquals(1.23D, snapshot.getPosition().getX(), 0.0D);
        Assert.assertEquals(-2.35D, snapshot.getPosition().getZ(), 0.0D);

        PlayerStats stats = snapshot.getStats();
        Assert.assertEquals(2, stats.getPlaytimeSeconds());
        Assert.assertEquals(2, stats.getDeaths());
        Assert.assertEquals(5, (int) stats.getMobKillsByType().get("zombie"));
        Assert.assertEquals(8, stats.getBlocksMined());
        Assert.assertEquals(19.88D, stats.getHealth(), 0.0D);
    }

    @Test
    public void privacyPolicyReplacesOnlyPrivateValues() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("privacy.send-private-data", false);
        yaml.set("privacy.fake-world", "hidden");
        yaml.set("privacy.fake-coordinate", 42.0D);
        yaml.set("privacy.fake-health", 10.0D);
        yaml.set("privacy.fake-food", 9);
        yaml.set("privacy.fake-level", 8);
        yaml.set("privacy.fake-exp", 0.25D);
        PrivacyPolicy privacy = SyncConfig.from(yaml).getPrivacyPolicy();
        Player player = TestPlayers.player("Steve", List.of());
        PlayerPrivacyMapper mapper = new PlayerPrivacyMapper();

        Assert.assertEquals("hidden", mapper.worldName(player.getWorld(), privacy));
        Assert.assertEquals(42.0D, mapper.position(player.getLocation(), privacy).getX(), 0.0D);
        Assert.assertEquals(10.0D, mapper.health(player, privacy), 0.0D);
        Assert.assertEquals(9, mapper.food(player, privacy));
        Assert.assertEquals(8, mapper.level(player, privacy));
        Assert.assertEquals(0.25D, mapper.exp(player, privacy), 0.0D);
    }

    @Test
    public void statisticReaderHandlesUnknownStatistic() {
        StatisticReader reader = new StatisticReader();

        Assert.assertNull(reader.find("DOES_NOT_EXIST"));
        Assert.assertEquals(0, reader.read(TestPlayers.player("Steve", List.of()), "DOES_NOT_EXIST"));
    }
}
