package ru.nyansus.mc.fallenlink.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final DateTimeFormatter MYSQL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private TimeUtil() {
    }

    public static String nowMysql(Clock clock, ZoneId zoneId) {
        return MYSQL_FORMATTER.format(clock.instant().atZone(zoneId));
    }

    public static String mysqlFromMillis(long millis, ZoneId zoneId) {
        return MYSQL_FORMATTER.format(Instant.ofEpochMilli(millis).atZone(zoneId));
    }

    public static String nowIso(Clock clock) {
        return ISO_FORMATTER.format(clock.instant().atZone(ZoneOffset.UTC));
    }
}
