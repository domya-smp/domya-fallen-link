package ru.nyansus.mc.fallenlink.player;

import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.config.PrivacyPolicy;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.model.PlayerStats;
import ru.nyansus.mc.fallenlink.model.Position;
import ru.nyansus.mc.fallenlink.util.TimeUtil;

public final class PlayerSnapshotFactory {

    private final PlayerNameResolver nameResolver;
    private final SyncConfig config;

    public PlayerSnapshotFactory(PlayerNameResolver nameResolver, SyncConfig config) {
        this.nameResolver = nameResolver;
        this.config = config;
    }

    public PlayerSnapshot create(Player player, boolean online) {
        Location location = player.getLocation();
        World world = player.getWorld();
        String publicName = nameResolver.resolve(player, config);
        PrivacyPolicy privacy = config.getPrivacyPolicy();
        return new PlayerSnapshot(
                player.getUniqueId().toString(),
                publicName,
                publicName,
                online,
                TimeUtil.nowMysql(),
                TimeUtil.mysqlFromMillis(Math.max(0L, player.getFirstPlayed())),
                worldName(world, privacy),
                position(location, privacy),
                stats(player, privacy)
        );
    }

    private String worldName(World world, PrivacyPolicy privacy) {
        if (!privacy.isSendPrivateData()) {
            return privacy.getFakeWorld();
        }
        return world == null ? "" : world.getName();
    }

    private Position position(Location location, PrivacyPolicy privacy) {
        if (!privacy.isSendPrivateData()) {
            double coordinate = privacy.getFakeCoordinate();
            return new Position(coordinate, coordinate, coordinate);
        }
        return new Position(
                location == null ? 0.0D : round(location.getX()),
                location == null ? 0.0D : round(location.getY()),
                location == null ? 0.0D : round(location.getZ())
        );
    }

    private PlayerStats stats(Player player, PrivacyPolicy privacy) {
        return new PlayerStats(
                ticksToSeconds(stat(player, "PLAY_ONE_MINUTE", "PLAY_TIME")),
                stat(player, "DEATHS"),
                stat(player, "PLAYER_KILLS"),
                stat(player, "MOB_KILLS"),
                mobKillsByType(player),
                stat(player, "JUMP"),
                stat(player, "DAMAGE_DEALT"),
                stat(player, "DAMAGE_TAKEN"),
                distanceCm(player),
                minedBlocks(player),
                stat(player, "ANIMALS_BRED"),
                stat(player, "FISH_CAUGHT"),
                privacy.isSendPrivateData() ? player.getLevel() : privacy.getFakeLevel(),
                privacy.isSendPrivateData() ? round(player.getExp()) : privacy.getFakeExp(),
                privacy.isSendPrivateData() ? round(player.getHealth()) : privacy.getFakeHealth(),
                privacy.isSendPrivateData() ? player.getFoodLevel() : privacy.getFakeFood()
        );
    }

    private int distanceCm(Player player) {
        return stat(player,
                "WALK_ONE_CM",
                "SPRINT_ONE_CM",
                "CROUCH_ONE_CM",
                "SWIM_ONE_CM",
                "FALL_ONE_CM",
                "CLIMB_ONE_CM",
                "FLY_ONE_CM",
                "BOAT_ONE_CM",
                "MINECART_ONE_CM",
                "PIG_ONE_CM",
                "HORSE_ONE_CM",
                "AVIATE_ONE_CM");
    }

    private int minedBlocks(Player player) {
        Statistic statistic = statistic("MINE_BLOCK");
        if (statistic == null) {
            return 0;
        }

        int total = 0;
        for (Material material : Material.values()) {
            if (!material.isBlock()) {
                continue;
            }
            try {
                total += player.getStatistic(statistic, material);
            } catch (RuntimeException e) {
                // Some server versions reject materials without a matching statistic.
            }
        }
        return total;
    }

    private Map<String, Integer> mobKillsByType(Player player) {
        Statistic statistic = statistic("KILL_ENTITY");
        if (statistic == null) {
            return Map.of();
        }

        Map<String, Integer> kills = new TreeMap<>();
        for (EntityType entityType : EntityType.values()) {
            if (!isMobKillStatCandidate(entityType)) {
                continue;
            }
            int count = entityStatistic(player, statistic, entityType);
            if (count > 0) {
                kills.put(entityType.getKey().getKey(), count);
            }
        }
        return kills;
    }

    private boolean isMobKillStatCandidate(EntityType entityType) {
        return entityType.isAlive()
                && entityType != EntityType.PLAYER
                && entityType.getKey() != null;
    }

    private int entityStatistic(Player player, Statistic statistic, EntityType entityType) {
        try {
            return player.getStatistic(statistic, entityType);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private int stat(Player player, String... names) {
        for (String name : names) {
            Statistic statistic = statistic(name);
            if (statistic == null) {
                continue;
            }
            try {
                return player.getStatistic(statistic);
            } catch (RuntimeException e) {
                return 0;
            }
        }
        return 0;
    }

    private Statistic statistic(String name) {
        try {
            return Statistic.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int ticksToSeconds(int ticks) {
        return ticks / 20;
    }

    private double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

}
