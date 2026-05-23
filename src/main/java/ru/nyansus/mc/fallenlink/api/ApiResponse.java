package ru.nyansus.mc.fallenlink.api;

public final class ApiResponse {

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

    public boolean isOkJson() {
        return isSuccessful() && body.contains("\"ok\":true");
    }
}
