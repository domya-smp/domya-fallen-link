package ru.nyansus.mc.fallenlink.config;

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
        Assert.assertTrue(config.isUsePlaceholderApiName());
        Assert.assertEquals("", config.getNamePlaceholder());
        Assert.assertTrue(config.getPrivacyPolicy().isSendPrivateData());
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

    @Test
    public void privacyPolicyReadsFakeValues() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("privacy.send-private-data", false);
        yaml.set("privacy.fake-world", "hidden");
        yaml.set("privacy.fake-coordinate", 42.0D);
        yaml.set("privacy.fake-health", 19.0D);
        yaml.set("privacy.fake-food", 18);
        yaml.set("privacy.fake-level", 7);
        yaml.set("privacy.fake-exp", 0.25D);

        PrivacyPolicy privacy = SyncConfig.from(yaml).getPrivacyPolicy();

        Assert.assertFalse(privacy.isSendPrivateData());
        Assert.assertEquals("hidden", privacy.getFakeWorld());
        Assert.assertEquals(42.0D, privacy.getFakeCoordinate(), 0.0D);
        Assert.assertEquals(19.0D, privacy.getFakeHealth(), 0.0D);
        Assert.assertEquals(18, privacy.getFakeFood());
        Assert.assertEquals(7, privacy.getFakeLevel());
        Assert.assertEquals(0.25D, privacy.getFakeExp(), 0.0D);
    }
}
