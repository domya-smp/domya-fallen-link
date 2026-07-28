package ru.nyansus.mc.fallenlink.api;

import org.junit.Assert;
import org.junit.Test;

public final class ApiResponseTest {

    @Test
    public void okJsonRequiresHttpSuccessAndOkTrue() {
        Assert.assertTrue(new ApiResponse(200, "{\"ok\":true}").isOkJson());
        Assert.assertTrue(new ApiResponse(201, "{ \"ok\" : true }").isOkJson());
        Assert.assertFalse(new ApiResponse(500, "{\"ok\":true}").isOkJson());
        Assert.assertFalse(new ApiResponse(200, "{\"ok\":false}").isOkJson());
        Assert.assertFalse(new ApiResponse(200, "{\"not_ok\":true}").isOkJson());
    }

    @Test
    public void zeroStatusRepresentsConnectionError() {
        Assert.assertTrue(new ApiResponse(0, null).isConnectionError());
        Assert.assertFalse(new ApiResponse(500, "").isConnectionError());
        Assert.assertEquals("", new ApiResponse(200, null).getBody());
    }
}
