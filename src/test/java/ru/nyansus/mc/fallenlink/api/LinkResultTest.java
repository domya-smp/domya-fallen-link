package ru.nyansus.mc.fallenlink.api;

import org.junit.Assert;
import org.junit.Test;

public final class LinkResultTest {

    @Test
    public void mapsApiResponsesToExplicitStatuses() {
        Assert.assertEquals(
                LinkResult.Status.SUCCESS,
                LinkResult.from(new ApiResponse(200, "{\"ok\":true}")).getStatus()
        );
        Assert.assertEquals(
                LinkResult.Status.HTTP_ERROR,
                LinkResult.from(new ApiResponse(400, "bad code")).getStatus()
        );
        Assert.assertEquals(
                LinkResult.Status.CONNECTION_ERROR,
                LinkResult.from(new ApiResponse(0, "")).getStatus()
        );
        Assert.assertEquals(LinkResult.Status.NOT_CONFIGURED, LinkResult.notConfigured().getStatus());
    }

    @Test
    public void preservesHttpDiagnostics() {
        LinkResult result = LinkResult.from(new ApiResponse(422, null));

        Assert.assertEquals(422, result.getStatusCode());
        Assert.assertEquals("", result.getBody());
    }
}
