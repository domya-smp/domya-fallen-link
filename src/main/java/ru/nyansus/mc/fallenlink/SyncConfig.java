package ru.nyansus.mc.fallenlink;

import org.bukkit.configuration.file.FileConfiguration;

public final class SyncConfig {

    private final String apiUrl;
    private final String linkUrl;
    private final String secretToken;
    private final long syncIntervalSeconds;
    private final boolean syncOnJoin;
    private final boolean syncOnQuit;
    private final boolean debug;
    private final boolean usePlaceholderApiName;
    private final String namePlaceholder;
    private final PrivacyPolicy privacyPolicy;

    private SyncConfig(
            String apiUrl,
            String linkUrl,
            String secretToken,
            long syncIntervalSeconds,
            boolean syncOnJoin,
            boolean syncOnQuit,
            boolean debug,
            boolean usePlaceholderApiName,
            String namePlaceholder,
            PrivacyPolicy privacyPolicy
    ) {
        this.apiUrl = apiUrl;
        this.linkUrl = linkUrl;
        this.secretToken = secretToken;
        this.syncIntervalSeconds = syncIntervalSeconds;
        this.syncOnJoin = syncOnJoin;
        this.syncOnQuit = syncOnQuit;
        this.debug = debug;
        this.usePlaceholderApiName = usePlaceholderApiName;
        this.namePlaceholder = namePlaceholder;
        this.privacyPolicy = privacyPolicy;
    }

    public static SyncConfig from(FileConfiguration config) {
        return new SyncConfig(
                string(config, "api-url"),
                string(config, "link-url"),
                string(config, "secret-token"),
                Math.max(15L, config.getLong("sync-interval-seconds", 60L)),
                config.getBoolean("sync-on-join", true),
                config.getBoolean("sync-on-quit", true),
                config.getBoolean("debug", false),
                config.getBoolean("display-name.use-placeholderapi", true),
                string(config, "display-name.placeholder"),
                PrivacyPolicy.from(config)
        );
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public long getSyncIntervalSeconds() {
        return syncIntervalSeconds;
    }

    public boolean isSyncOnJoin() {
        return syncOnJoin;
    }

    public boolean isSyncOnQuit() {
        return syncOnQuit;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isUsePlaceholderApiName() {
        return usePlaceholderApiName;
    }

    public String getNamePlaceholder() {
        return namePlaceholder;
    }

    public PrivacyPolicy getPrivacyPolicy() {
        return privacyPolicy;
    }

    public boolean hasSyncSettings() {
        return apiUrl.length() >= 8 && hasSecretToken();
    }

    public boolean hasLinkSettings() {
        return linkUrl.length() >= 8 && hasSecretToken();
    }

    private boolean hasSecretToken() {
        return secretToken.length() >= 12 && !"CHANGE_ME".equals(secretToken);
    }

    private static String string(FileConfiguration config, String key) {
        return config.getString(key, "").trim();
    }
}
