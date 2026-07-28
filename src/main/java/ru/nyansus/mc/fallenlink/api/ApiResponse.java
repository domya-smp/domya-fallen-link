package ru.nyansus.mc.fallenlink.api;

import java.util.regex.Pattern;

public final class ApiResponse {

    private static final Pattern OK_TRUE_PATTERN = Pattern.compile("\"ok\"\\s*:\\s*true");

    private final int statusCode;
    private final String body;

    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body == null ? "" : body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isConnectionError() {
        return statusCode == 0;
    }

    public boolean isOkJson() {
        return isSuccessful() && OK_TRUE_PATTERN.matcher(body).find();
    }
}
