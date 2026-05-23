package ru.nyansus.mc.fallenlink;

import org.bukkit.entity.Player;

public final class PlayerLinkRequest {

    private final String code;
    private final String uuid;
    private final String nickname;
    private final String displayName;

    private PlayerLinkRequest(String code, String uuid, String nickname, String displayName) {
        this.code = code;
        this.uuid = uuid;
        this.nickname = nickname;
        this.displayName = displayName;
    }

    public static PlayerLinkRequest from(Player player, String code) {
        return new PlayerLinkRequest(
                code,
                player.getUniqueId().toString(),
                player.getName(),
                PlayerSnapshotFactory.safeDisplayName(player)
        );
    }

    public String getCode() {
        return code;
    }

    public String getUuid() {
        return uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDisplayName() {
        return displayName;
    }
}
