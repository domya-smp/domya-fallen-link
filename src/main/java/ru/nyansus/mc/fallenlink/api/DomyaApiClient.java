package ru.nyansus.mc.fallenlink.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.config.SyncConfigProvider;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;

public final class DomyaApiClient implements DomyaGateway {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(8);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(12);

    private final HttpClient httpClient;
    private final Logger logger;
    private final MessageProvider messages;
    private final SyncConfigProvider configProvider;
    private final DomyaPayloadFactory payloadFactory;
    private final String userAgent;

    public DomyaApiClient(
            Logger logger,
            MessageProvider messages,
            SyncConfigProvider configProvider,
            DomyaPayloadFactory payloadFactory,
            String pluginVersion
    ) {
        this.logger = logger;
        this.messages = messages;
        this.configProvider = configProvider;
        this.payloadFactory = payloadFactory;
        this.userAgent = "domya-fallen-link/" + pluginVersion + " Paper";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    @Override
    public CompletableFuture<ApiResponse> syncPlayers(Collection<PlayerSnapshot> players) {
        SyncConfig config = configProvider.current();
        if (!config.hasSyncSettings()) {
            logger.warning(messages.get("log.sync-not-configured"));
            return CompletableFuture.completedFuture(new ApiResponse(0, ""));
        }

        String payload = payloadFactory.syncPayload(config.getSecretToken(), players);
        return postJson(config.getApiUrl(), payload, "sync");
    }

    @Override
    public CompletableFuture<LinkResult> linkPlayer(PlayerLinkRequest request) {
        SyncConfig config = configProvider.current();
        if (!config.hasLinkSettings()) {
            return CompletableFuture.completedFuture(LinkResult.notConfigured());
        }

        String payload = payloadFactory.linkPayload(config.getSecretToken(), request);
        return postJson(config.getLinkUrl(), payload, "link").thenApply(LinkResult::from);
    }

    @Override
    public void close() {
        httpClient.close();
    }

    private CompletableFuture<ApiResponse> postJson(String url, String payload, String operation) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("User-Agent", userAgent)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
        } catch (IllegalArgumentException error) {
            return CompletableFuture.completedFuture(handleError(operation, error));
        }

        if (configProvider.current().isDebug()) {
            logger.info(messages.get("log.api-request",
                    "{operation}", operation,
                    "{payload}", "[redacted]"));
        }

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleResponse(operation, response))
                .exceptionally(error -> handleError(operation, error));
    }

    private ApiResponse handleResponse(String operation, HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        if (configProvider.current().isDebug() || response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.info(messages.get("log.api-response",
                    "{operation}", operation,
                    "{status}", String.valueOf(response.statusCode()),
                    "{body}", body));
        }
        return new ApiResponse(response.statusCode(), body);
    }

    private ApiResponse handleError(String operation, Throwable error) {
        Throwable cause = error;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        logger.log(Level.WARNING, messages.get("log.api-request-failed", "{operation}", operation), cause);
        return new ApiResponse(0, "");
    }
}
