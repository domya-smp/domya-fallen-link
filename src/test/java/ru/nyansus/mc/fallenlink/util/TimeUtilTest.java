package ru.nyansus.mc.fallenlink.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.Assert;
import org.junit.Test;

public final class TimeUtilTest {

    @Test
    public void formatsInjectedTimeDeterministically() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T10:15:30Z"), ZoneOffset.UTC);

        Assert.assertEquals("2026-07-28T10:15:30Z", TimeUtil.nowIso(clock));
        Assert.assertEquals(
                "2026-07-28 16:15:30",
                TimeUtil.nowMysql(clock, ZoneId.of("Asia/Omsk"))
        );
        Assert.assertEquals(
                "1970-01-01 06:00:00",
                TimeUtil.mysqlFromMillis(0, ZoneId.of("Asia/Omsk"))
        );
    }
}
