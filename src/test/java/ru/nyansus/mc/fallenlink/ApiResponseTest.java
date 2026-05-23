package ru.nyansus.mc.fallenlink;

import org.junit.Assert;
import org.junit.Test;

public final class ApiResponseTest {

    @Test
    public void okJsonRequiresHttpSuccessAndOkTrue() {
        Assert.assertTrue(new ApiResponse(200, "{\"ok\":true}").isOkJson());
        Assert.assertFalse(new ApiResponse(500, "{\"ok\":true}").isOkJson());
        Assert.assertFalse(new ApiResponse(200, "{\"ok\":false}").isOkJson());
    }
}
