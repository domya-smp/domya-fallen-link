package ru.nyansus.mc.fallenlink.api;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.model.PlayerStats;
import ru.nyansus.mc.fallenlink.model.Position;
import ru.nyansus.mc.fallenlink.serialization.SnapshotJsonSerializer;

public final class DomyaPayloadFactoryTest {

    @Test
    public void syncPayloadKeepsCompatibleShape() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T10:15:30Z"), ZoneOffset.UTC);
        DomyaPayloadFactory factory = new DomyaPayloadFactory(new SnapshotJsonSerializer(), clock);

        String payload = factory.syncPayload("secret", "[{\"uuid\":\"u\"}]");

        Assert.assertEquals(
                "{\"token\":\"secret\",\"source\":\"spigot\",\"server_time\":\"2026-07-28T10:15:30Z\","
                        + "\"players\":[{\"uuid\":\"u\"}]}",
                payload
        );
    }

    @Test
    public void syncPayloadSerializesSnapshotsInsideGatewayBoundary() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T10:15:30Z"), ZoneOffset.UTC);
        DomyaPayloadFactory factory = new DomyaPayloadFactory(new SnapshotJsonSerializer(), clock);
        PlayerStats stats = new PlayerStats(
                1, 2, 3, 4, Map.of(), 5, 6, 7, 8, 9, 10, 11, 12, 0.5D, 20.0D, 19);
        PlayerSnapshot snapshot = new PlayerSnapshot(
                "u", "n", "d", true, "last", "first", "world", new Position(1, 2, 3), stats);

        String payload = factory.syncPayload("secret", List.of(snapshot));

        Assert.assertTrue(payload.contains("\"players\":[{\"uuid\":\"u\""));
        Assert.assertTrue(payload.contains("\"server_time\":\"2026-07-28T10:15:30Z\""));
    }

    @Test
    public void linkPayloadKeepsCompatibleShape() {
        DomyaPayloadFactory factory = new DomyaPayloadFactory();
        PlayerLinkRequest request = new PlayerLinkRequest("code", "uuid", "nick", "display");

        String payload = factory.linkPayload("secret", request);

        Assert.assertEquals(
                "{\"token\":\"secret\",\"code\":\"code\",\"uuid\":\"uuid\","
                        + "\"nickname\":\"nick\",\"display_name\":\"display\"}",
                payload
        );
    }
}
