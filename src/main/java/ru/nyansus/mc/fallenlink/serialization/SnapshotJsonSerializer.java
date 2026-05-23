package ru.nyansus.mc.fallenlink.serialization;

import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.model.PlayerStats;
import ru.nyansus.mc.fallenlink.model.Position;

public final class SnapshotJsonSerializer {

    public String serialize(PlayerSnapshot snapshot) {
        JsonWriter writer = new JsonWriter();
        writer.beginObject();
        writer.field("uuid", snapshot.getUuid());
        writer.field("nickname", snapshot.getNickname());
        writer.field("display_name", snapshot.getDisplayName());
        writer.field("is_online", snapshot.isOnline());
        writer.field("last_seen_at", snapshot.getLastSeenAt());
        writer.field("first_seen_at", snapshot.getFirstSeenAt());
        writer.field("world", snapshot.getWorld());
        writePosition(writer, snapshot.getPosition());
        writeStats(writer, snapshot.getStats());
        writer.endObject();
        return writer.toString();
    }

    private void writePosition(JsonWriter writer, Position position) {
        writer.name("location");
        writer.beginObject();
        writer.field("x", position.getX());
        writer.field("y", position.getY());
        writer.field("z", position.getZ());
        writer.endObject();
    }

    private void writeStats(JsonWriter writer, PlayerStats stats) {
        writer.name("stats");
        writer.beginObject();
        writer.field("playtime_seconds", stats.getPlaytimeSeconds());
        writer.field("deaths", stats.getDeaths());
        writer.field("player_kills", stats.getPlayerKills());
        writer.field("mob_kills", stats.getMobKills());
        writer.field("jumps", stats.getJumps());
        writer.field("damage_dealt", stats.getDamageDealt());
        writer.field("damage_taken", stats.getDamageTaken());
        writer.field("distance_cm", stats.getDistanceCm());
        writer.field("blocks_mined", stats.getBlocksMined());
        writer.field("animals_bred", stats.getAnimalsBred());
        writer.field("fish_caught", stats.getFishCaught());
        writer.field("level", stats.getLevel());
        writer.field("exp", stats.getExp());
        writer.field("health", stats.getHealth());
        writer.field("food", stats.getFood());
        writer.endObject();
    }
}
