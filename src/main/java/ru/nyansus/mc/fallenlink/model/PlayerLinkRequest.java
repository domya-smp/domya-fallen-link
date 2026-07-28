package ru.nyansus.mc.fallenlink.model;

public final class PlayerLinkRequest {

    private final String code;
    private final String uuid;
    private final String nickname;
    private final String displayName;

    public PlayerLinkRequest(String code, String uuid, String nickname, String displayName) {
        this.code = code;
        this.uuid = uuid;
        this.nickname = nickname;
        this.displayName = displayName;
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
