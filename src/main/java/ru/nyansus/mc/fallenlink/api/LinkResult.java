package ru.nyansus.mc.fallenlink.api;

public final class LinkResult {

    public enum Status {
        SUCCESS,
        HTTP_ERROR,
        CONNECTION_ERROR,
        NOT_CONFIGURED
    }

    private final Status status;
    private final int statusCode;
    private final String body;

    private LinkResult(Status status, int statusCode, String body) {
        this.status = status;
        this.statusCode = statusCode;
        this.body = body == null ? "" : body;
    }

    public static LinkResult from(ApiResponse response) {
        if (response.isConnectionError()) {
            return new LinkResult(Status.CONNECTION_ERROR, response.getStatusCode(), response.getBody());
        }
        if (response.isOkJson()) {
            return new LinkResult(Status.SUCCESS, response.getStatusCode(), response.getBody());
        }
        return new LinkResult(Status.HTTP_ERROR, response.getStatusCode(), response.getBody());
    }

    public static LinkResult notConfigured() {
        return new LinkResult(Status.NOT_CONFIGURED, 0, "");
    }

    public Status getStatus() {
        return status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
