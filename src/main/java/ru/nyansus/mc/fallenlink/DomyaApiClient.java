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
    private final Messages messages;
    private final SyncConfig config;
    private final DomyaPayloadFactory payloadFactory;

    public DomyaApiClient(
            Logger logger,
            Messages messages,
            SyncConfig config,
            DomyaPayloadFactory payloadFactory
    ) {
        this.logger = logger;
        this.messages = messages;
        this.config = config;
        this.payloadFactory = payloadFactory;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    public CompletableFuture<ApiResponse> sendSyncPayload(String playersJson) {
        if (!config.hasSyncSettings()) {
            logger.warning(messages.get("log.sync-not-configured"));
            return CompletableFuture.completedFuture(new ApiResponse(0, ""));
        }

        String payload = payloadFactory.syncPayload(config.getSecretToken(), playersJson);
        return postJson(config.getApiUrl(), payload, "sync");
    }

    public CompletableFuture<ApiResponse> sendLinkRequest(PlayerLinkRequest request) {
        if (!config.hasLinkSettings()) {
            return CompletableFuture.completedFuture(new ApiResponse(0, ""));
        }

        String payload = payloadFactory.linkPayload(config.getSecretToken(), request);
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
            logger.info(messages.get("log.api-request",
                    "{operation}", operation,
                    "{payload}", payload));
        }

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleResponse(operation, response))
                .exceptionally(error -> handleError(operation, error));
    }

    private ApiResponse handleResponse(String operation, HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        if (config.isDebug() || response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.info(messages.get("log.api-response",
                    "{operation}", operation,
                    "{status}", String.valueOf(response.statusCode()),
                    "{body}", body));
        }
        return new ApiResponse(response.statusCode(), body);
    }

    private ApiResponse handleError(String operation, Throwable error) {
        Throwable cause = error instanceof IOException ? error : error.getCause();
        if (cause == null) {
            cause = error;
        }
        logger.log(Level.WARNING, messages.get("log.api-request-failed", "{operation}", operation), cause);
        return new ApiResponse(0, "");
    }
}
