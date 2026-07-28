package ru.nyansus.mc.fallenlink.player;

import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.config.PrivacyPolicy;
import ru.nyansus.mc.fallenlink.model.PlayerStats;

public final class PlayerStatisticsCollector {

    private final StatisticReader statisticReader;
    private final PlayerPrivacyMapper privacyMapper;
    private final StatisticKeyCatalog statisticKeyCatalog;

    public PlayerStatisticsCollector(
            StatisticReader statisticReader,
            PlayerPrivacyMapper privacyMapper,
            StatisticKeyCatalog statisticKeyCatalog
    ) {
        this.statisticReader = statisticReader;
        this.privacyMapper = privacyMapper;
        this.statisticKeyCatalog = statisticKeyCatalog;
    }

    public PlayerStats collect(Player player, PrivacyPolicy privacy) {
        return new PlayerStats(
                ticksToSeconds(statisticReader.read(player, "PLAY_ONE_MINUTE", "PLAY_TIME")),
                statisticReader.read(player, "DEATHS"),
                statisticReader.read(player, "PLAYER_KILLS"),
                statisticReader.read(player, "MOB_KILLS"),
                mobKillsByType(player),
                statisticReader.read(player, "JUMP"),
                statisticReader.read(player, "DAMAGE_DEALT"),
                statisticReader.read(player, "DAMAGE_TAKEN"),
                distanceCm(player),
                minedBlocks(player),
                statisticReader.read(player, "ANIMALS_BRED"),
                statisticReader.read(player, "FISH_CAUGHT"),
                privacyMapper.level(player, privacy),
                privacyMapper.exp(player, privacy),
                privacyMapper.health(player, privacy),
                privacyMapper.food(player, privacy)
        );
    }

    private int distanceCm(Player player) {
        return statisticReader.read(player,
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
        Statistic statistic = statisticReader.find("MINE_BLOCK");
        if (statistic == null) {
            return 0;
        }

        int total = 0;
        for (Material material : statisticKeyCatalog.blockMaterials()) {
            total += statisticReader.read(player, statistic, material);
        }
        return total;
    }

    private Map<String, Integer> mobKillsByType(Player player) {
        Statistic statistic = statisticReader.find("KILL_ENTITY");
        if (statistic == null) {
            return Map.of();
        }

        Map<String, Integer> kills = new TreeMap<>();
        for (EntityType entityType : statisticKeyCatalog.mobTypes()) {
            int count = statisticReader.read(player, statistic, entityType);
            if (count > 0) {
                kills.put(entityType.getKey().getKey(), count);
            }
        }
        return kills;
    }

    private int ticksToSeconds(int ticks) {
        return ticks / 20;
    }
}
