package ru.nyansus.mc.fallenlink.player;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public final class StatisticReader {

    public int read(Player player, String... names) {
        for (String name : names) {
            Statistic statistic = find(name);
            if (statistic == null) {
                continue;
            }
            try {
                return player.getStatistic(statistic);
            } catch (RuntimeException error) {
                return 0;
            }
        }
        return 0;
    }

    public int read(Player player, Statistic statistic, Material material) {
        try {
            return player.getStatistic(statistic, material);
        } catch (RuntimeException error) {
            return 0;
        }
    }

    public int read(Player player, Statistic statistic, EntityType entityType) {
        try {
            return player.getStatistic(statistic, entityType);
        } catch (RuntimeException error) {
            return 0;
        }
    }

    public Statistic find(String name) {
        try {
            return Statistic.valueOf(name);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }
}
