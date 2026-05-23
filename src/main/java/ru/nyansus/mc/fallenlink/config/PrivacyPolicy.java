package ru.nyansus.mc.fallenlink.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class PrivacyPolicy {

    private final boolean sendPrivateData;
    private final String fakeWorld;
    private final double fakeCoordinate;
    private final double fakeHealth;
    private final int fakeFood;
    private final int fakeLevel;
    private final double fakeExp;

    private PrivacyPolicy(
            boolean sendPrivateData,
            String fakeWorld,
            double fakeCoordinate,
            double fakeHealth,
            int fakeFood,
            int fakeLevel,
            double fakeExp
    ) {
        this.sendPrivateData = sendPrivateData;
        this.fakeWorld = fakeWorld;
        this.fakeCoordinate = fakeCoordinate;
        this.fakeHealth = fakeHealth;
        this.fakeFood = fakeFood;
        this.fakeLevel = fakeLevel;
        this.fakeExp = fakeExp;
    }

    public static PrivacyPolicy from(FileConfiguration config) {
        return new PrivacyPolicy(
                config.getBoolean("privacy.send-private-data", true),
                config.getString("privacy.fake-world", ""),
                config.getDouble("privacy.fake-coordinate", 0.0D),
                config.getDouble("privacy.fake-health", 20.0D),
                config.getInt("privacy.fake-food", 20),
                config.getInt("privacy.fake-level", 0),
                config.getDouble("privacy.fake-exp", 0.0D)
        );
    }

    public boolean isSendPrivateData() {
        return sendPrivateData;
    }

    public String getFakeWorld() {
        return fakeWorld;
    }

    public double getFakeCoordinate() {
        return fakeCoordinate;
    }

    public double getFakeHealth() {
        return fakeHealth;
    }

    public int getFakeFood() {
        return fakeFood;
    }

    public int getFakeLevel() {
        return fakeLevel;
    }

    public double getFakeExp() {
        return fakeExp;
    }
}
