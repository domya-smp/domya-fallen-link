package ru.nyansus.mc.fallenlink;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DomyaApiClient implements AutoCloseable {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(8);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(12);

    private final HttpClient httpClient;
    private final Logger logger;
    private final SyncConfig config;

    public DomyaApiClient(Logger logger, SyncConfig config) {
        this.logger = logger;
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    public CompletableFuture<ApiResponse> sendSyncPayload(String playersJson) {
        if (!config.hasSyncSettings()) {
            logger.warning("API URL or secret token is not configured.");
            return CompletableFuture.completedFuture(new ApiResponse(0, ""));
        }

        String payload = "{\"token\":" + JsonWriter.quote(config.getSecretToken())
                + ",\"source\":\"spigot\""
                + ",\"server_time\":" + JsonWriter.quote(TimeUtil.nowIso())
                + ",\"players\":" + playersJson
                + "}";
        return postJson(config.getApiUrl(), payload, "sync");
    }

    public CompletableFuture<ApiResponse> sendLinkRequest(PlayerLinkRequest request) {
        if (!config.hasLinkSettings()) {
            return CompletableFuture.completedFuture(new ApiResponse(0, ""));
        }

        String payload = "{\"token\":" + JsonWriter.quote(config.getSecretToken())
                + ",\"code\":" + JsonWriter.quote(request.getCode())
                + ",\"uuid\":" + JsonWriter.quote(request.getUuid())
                + ",\"nickname\":" + JsonWriter.quote(request.getNickname())
                + ",\"display_name\":" + JsonWriter.quote(request.getDisplayName())
                + "}";
        return postJson(config.getLinkUrl(), payload, "link");
    }

    @Override
    public void close() {
        httpClient.close();
    }

    private CompletableFuture<ApiResponse> postJson(String url, String payload, String operation) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("User-Agent", "domya-fallen-link/1.0.0 Paper")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        if (config.isDebug()) {
            logger.info("Domya " + operation + " request: " + payload);
        }

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleResponse(operation, response))
                .exceptionally(error -> handleError(operation, error));
    }

    private ApiResponse handleResponse(String operation, HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        if (config.isDebug() || response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.info("Domya " + operation + " response HTTP " + response.statusCode() + ": " + body);
        }
        return new ApiResponse(response.statusCode(), body);
    }

    private ApiResponse handleError(String operation, Throwable error) {
        Throwable cause = error instanceof IOException ? error : error.getCause();
        if (cause == null) {
            cause = error;
        }
        logger.log(Level.WARNING, "Domya " + operation + " request failed", cause);
        return new ApiResponse(0, "");
    }
}
