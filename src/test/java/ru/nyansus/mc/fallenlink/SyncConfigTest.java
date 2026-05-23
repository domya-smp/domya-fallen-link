package ru.nyansus.mc.fallenlink;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;

public final class SyncConfigTest {

    @Test
    public void fromKeepsCompatibleDefaults() {
        YamlConfiguration yaml = new YamlConfiguration();

        SyncConfig config = SyncConfig.from(yaml);

        Assert.assertEquals("", config.getApiUrl());
        Assert.assertEquals("", config.getLinkUrl());
        Assert.assertEquals("", config.getSecretToken());
        Assert.assertEquals(60L, config.getSyncIntervalSeconds());
        Assert.assertTrue(config.isSyncOnJoin());
        Assert.assertTrue(config.isSyncOnQuit());
        Assert.assertFalse(config.isDebug());
    }

    @Test
    public void fromClampsIntervalToFifteenSeconds() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("sync-interval-seconds", 1L);

        SyncConfig config = SyncConfig.from(yaml);

        Assert.assertEquals(15L, config.getSyncIntervalSeconds());
    }

    @Test
    public void placeholderTokenIsNotAConfiguredSecret() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("api-url", "https://example.test/sync");
        yaml.set("link-url", "https://example.test/link");
        yaml.set("secret-token", "CHANGE_ME");

        SyncConfig config = SyncConfig.from(yaml);

        Assert.assertFalse(config.hasSyncSettings());
        Assert.assertFalse(config.hasLinkSettings());
    }
}
