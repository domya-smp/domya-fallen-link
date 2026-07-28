package ru.nyansus.mc.fallenlink.api;

import java.time.Clock;
import java.util.Collection;
import java.util.stream.Collectors;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;
import ru.nyansus.mc.fallenlink.serialization.JsonWriter;
import ru.nyansus.mc.fallenlink.serialization.SnapshotJsonSerializer;
import ru.nyansus.mc.fallenlink.util.TimeUtil;

public final class DomyaPayloadFactory {

    private final SnapshotJsonSerializer snapshotSerializer;
    private final Clock clock;

    public DomyaPayloadFactory() {
        this(new SnapshotJsonSerializer(), Clock.systemUTC());
    }

    public DomyaPayloadFactory(SnapshotJsonSerializer snapshotSerializer, Clock clock) {
        this.snapshotSerializer = snapshotSerializer;
        this.clock = clock;
    }

    public String syncPayload(String token, Collection<PlayerSnapshot> players) {
        String playersJson = players.stream()
                .map(snapshotSerializer::serialize)
                .collect(Collectors.joining(",", "[", "]"));
        return syncPayload(token, playersJson);
    }

    public String syncPayload(String token, String playersJson) {
        JsonWriter writer = new JsonWriter();
        writer.beginObject();
        writer.field("token", token);
        writer.field("source", "spigot");
        writer.field("server_time", TimeUtil.nowIso(clock));
        writer.rawField("players", playersJson);
        writer.endObject();
        return writer.toString();
    }

    public String linkPayload(String token, PlayerLinkRequest request) {
        JsonWriter writer = new JsonWriter();
        writer.beginObject();
        writer.field("token", token);
        writer.field("code", request.getCode());
        writer.field("uuid", request.getUuid());
        writer.field("nickname", request.getNickname());
        writer.field("display_name", request.getDisplayName());
        writer.endObject();
        return writer.toString();
    }
}
