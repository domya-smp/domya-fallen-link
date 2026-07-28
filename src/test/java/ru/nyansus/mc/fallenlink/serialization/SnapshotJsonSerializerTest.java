package ru.nyansus.mc.fallenlink.serialization;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.model.PlayerStats;
import ru.nyansus.mc.fallenlink.model.Position;

public final class SnapshotJsonSerializerTest {

    @Test
    public void serializeKeepsCompatiblePlayerShape() {
        PlayerStats stats = new PlayerStats(
                1, 2, 3, 4, Map.of("creeper", 2), 5, 6, 7, 8, 9, 10, 11, 12, 0.5D, 20.0D, 19);
        PlayerSnapshot snapshot = new PlayerSnapshot(
                "uuid",
                "nick",
                "display",
                true,
                "2026-05-23 12:00:00",
                "2026-05-01 12:00:00",
                "world",
                new Position(1.0D, 2.0D, 3.0D),
                stats
        );

        String json = new SnapshotJsonSerializer().serialize(snapshot);

        Assert.assertTrue(json.contains("\"uuid\":\"uuid\""));
        Assert.assertTrue(json.contains("\"nickname\":\"nick\""));
        Assert.assertTrue(json.contains("\"is_online\":true"));
        Assert.assertTrue(json.contains("\"location\":{\"x\":1.0,\"y\":2.0,\"z\":3.0}"));
        Assert.assertTrue(json.contains("\"stats\":{"));
        Assert.assertTrue(json.contains("\"playtime_seconds\":1"));
        Assert.assertTrue(json.contains("\"mob_kills\":4"));
        Assert.assertTrue(json.contains("\"mob_kills_by_type\":{\"creeper\":2}"));
        Assert.assertTrue(json.contains("\"food\":19"));
    }
}
