package ru.nyansus.mc.fallenlink;

public final class PlayerSnapshot {

    private final String uuid;
    private final String nickname;
    private final String displayName;
    private final boolean online;
    private final String lastSeenAt;
    private final String firstSeenAt;
    private final String world;
    private final Position position;
    private final PlayerStats stats;

    public PlayerSnapshot(
            String uuid,
            String nickname,
            String displayName,
            boolean online,
            String lastSeenAt,
            String firstSeenAt,
            String world,
            Position position,
            PlayerStats stats
    ) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.displayName = displayName;
        this.online = online;
        this.lastSeenAt = lastSeenAt;
        this.firstSeenAt = firstSeenAt;
        this.world = world;
        this.position = position;
        this.stats = stats;
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

    public boolean isOnline() {
        return online;
    }

    public String getLastSeenAt() {
        return lastSeenAt;
    }

    public String getFirstSeenAt() {
        return firstSeenAt;
    }

    public String getWorld() {
        return world;
    }

    public Position getPosition() {
        return position;
    }

    public PlayerStats getStats() {
        return stats;
    }
}
