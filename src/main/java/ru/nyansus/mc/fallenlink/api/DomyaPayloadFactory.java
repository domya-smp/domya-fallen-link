package ru.nyansus.mc.fallenlink.api;

import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.serialization.JsonWriter;
import ru.nyansus.mc.fallenlink.util.TimeUtil;

public final class DomyaPayloadFactory {

    public String syncPayload(String token, String playersJson) {
        JsonWriter writer = new JsonWriter();
        writer.beginObject();
        writer.field("token", token);
        writer.field("source", "spigot");
        writer.field("server_time", TimeUtil.nowIso());
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
