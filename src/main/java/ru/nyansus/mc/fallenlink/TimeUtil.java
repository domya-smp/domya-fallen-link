package ru.nyansus.mc.fallenlink;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final DateTimeFormatter MYSQL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private TimeUtil() {
    }

    public static String nowMysql() {
        return MYSQL_FORMATTER.format(Instant.now().atZone(ZoneId.systemDefault()));
    }

    public static String mysqlFromMillis(long millis) {
        return MYSQL_FORMATTER.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()));
    }

    public static String nowIso() {
        return ISO_FORMATTER.format(Instant.now().atZone(ZoneOffset.UTC));
    }
}
