package ru.nyansus.mc.fallenlink;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class PlayerSnapshotFactory {

    public PlayerSnapshot create(Player player, boolean online) {
        Location location = player.getLocation();
        World world = player.getWorld();
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        add(builder, "uuid", player.getUniqueId().toString()).append(',');
        add(builder, "nickname", player.getName()).append(',');
        add(builder, "display_name", safeDisplayName(player)).append(',');
        add(builder, "is_online", online).append(',');
        add(builder, "last_seen_at", TimeUtil.nowMysql()).append(',');
        add(builder, "first_seen_at", TimeUtil.mysqlFromMillis(Math.max(0L, player.getFirstPlayed()))).append(',');
        add(builder, "world", world == null ? "" : world.getName()).append(',');
        appendLocation(builder, location);
        builder.append(',');
        appendStats(builder, player);
        builder.append('}');
        return new PlayerSnapshot(builder.toString());
    }

    public static String safeDisplayName(Player player) {
        try {
            return player.getDisplayName();
        } catch (RuntimeException e) {
            return player.getName();
        }
    }

    private void appendLocation(StringBuilder builder, Location location) {
        builder.append("\"location\":{");
        add(builder, "x", location == null ? 0.0D : round(location.getX())).append(',');
        add(builder, "y", location == null ? 0.0D : round(location.getY())).append(',');
        add(builder, "z", location == null ? 0.0D : round(location.getZ()));
        builder.append('}');
    }

    private void appendStats(StringBuilder builder, Player player) {
        builder.append("\"stats\":{");
        add(builder, "playtime_seconds", ticksToSeconds(stat(player, "PLAY_ONE_MINUTE", "PLAY_TIME"))).append(',');
        add(builder, "deaths", stat(player, "DEATHS")).append(',');
        add(builder, "player_kills", stat(player, "PLAYER_KILLS")).append(',');
        add(builder, "mob_kills", stat(player, "MOB_KILLS")).append(',');
        add(builder, "jumps", stat(player, "JUMP")).append(',');
        add(builder, "damage_dealt", stat(player, "DAMAGE_DEALT")).append(',');
        add(builder, "damage_taken", stat(player, "DAMAGE_TAKEN")).append(',');
        add(builder, "distance_cm", distanceCm(player)).append(',');
        add(builder, "blocks_mined", minedBlocks(player)).append(',');
        add(builder, "animals_bred", stat(player, "ANIMALS_BRED")).append(',');
        add(builder, "fish_caught", stat(player, "FISH_CAUGHT")).append(',');
        add(builder, "level", player.getLevel()).append(',');
        add(builder, "exp", round(player.getExp())).append(',');
        add(builder, "health", round(player.getHealth())).append(',');
        add(builder, "food", player.getFoodLevel());
        builder.append('}');
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

    private StringBuilder add(StringBuilder builder, String key, String value) {
        return builder.append(JsonWriter.quote(key)).append(':').append(JsonWriter.quote(value));
    }

    private StringBuilder add(StringBuilder builder, String key, boolean value) {
        return builder.append(JsonWriter.quote(key)).append(':').append(value ? "true" : "false");
    }

    private StringBuilder add(StringBuilder builder, String key, int value) {
        return builder.append(JsonWriter.quote(key)).append(':').append(value);
    }

    private StringBuilder add(StringBuilder builder, String key, double value) {
        return builder.append(JsonWriter.quote(key)).append(':').append(value);
    }
}
