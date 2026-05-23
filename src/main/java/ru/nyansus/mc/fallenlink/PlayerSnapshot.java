package ru.nyansus.mc.fallenlink;

public final class PlayerSnapshot {

    private final String json;

    public PlayerSnapshot(String json) {
        this.json = json;
    }

    public String toJson() {
        return json;
    }
}
