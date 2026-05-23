package ru.nyansus.mc.fallenlink;

import org.junit.Assert;
import org.junit.Test;

public final class DomyaPayloadFactoryTest {

    @Test
    public void syncPayloadKeepsCompatibleShape() {
        DomyaPayloadFactory factory = new DomyaPayloadFactory();

        String payload = factory.syncPayload("secret", "[{\"uuid\":\"u\"}]");

        Assert.assertTrue(payload.contains("\"token\":\"secret\""));
        Assert.assertTrue(payload.contains("\"source\":\"spigot\""));
        Assert.assertTrue(payload.contains("\"server_time\":"));
        Assert.assertTrue(payload.contains("\"players\":[{\"uuid\":\"u\"}]"));
    }

    @Test
    public void linkPayloadKeepsCompatibleShape() {
        DomyaPayloadFactory factory = new DomyaPayloadFactory();
        PlayerLinkRequest request = new PlayerLinkRequest("code", "uuid", "nick", "display");

        String payload = factory.linkPayload("secret", request);

        Assert.assertEquals(
                "{\"token\":\"secret\",\"code\":\"code\",\"uuid\":\"uuid\","
                        + "\"nickname\":\"nick\",\"display_name\":\"display\"}",
                payload
        );
    }
}
