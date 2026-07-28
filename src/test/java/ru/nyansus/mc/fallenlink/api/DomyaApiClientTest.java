package ru.nyansus.mc.fallenlink.api;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;

public final class DomyaApiClientTest {

    @Test
    public void sendsRequestButRedactsSecretFromDebugLog() throws IOException {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/link", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{ \"ok\" : true }".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        String token = "very-secret-token";
        SyncConfig config = configured("http://127.0.0.1:" + server.getAddress().getPort() + "/link", token);
        RecordingHandler handler = new RecordingHandler();
        Logger logger = Logger.getLogger("DomyaApiClientTest");
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        DomyaApiClient client = new DomyaApiClient(
                logger,
                new RecordingMessages(),
                () -> config,
                new DomyaPayloadFactory(),
                "test"
        );

        try {
            LinkResult result = client.linkPlayer(new PlayerLinkRequest("code", "uuid", "nick", "display")).join();

            Assert.assertEquals(LinkResult.Status.SUCCESS, result.getStatus());
            Assert.assertTrue(requestBody.get().contains("\"token\":\"" + token + "\""));
            Assert.assertTrue(handler.messages.stream().anyMatch(message -> message.contains("[redacted]")));
            Assert.assertTrue(handler.messages.stream().noneMatch(message -> message.contains(token)));
        } finally {
            client.close();
            server.stop(0);
            logger.removeHandler(handler);
        }
    }

    @Test
    public void reportsNotConfiguredAndInvalidUriExplicitly() {
        Logger logger = Logger.getLogger("DomyaApiClientInvalidUriTest");
        DomyaApiClient unconfiguredClient = new DomyaApiClient(
                logger,
                new RecordingMessages(),
                () -> SyncConfig.from(new YamlConfiguration()),
                new DomyaPayloadFactory(),
                "test"
        );
        DomyaApiClient invalidUriClient = new DomyaApiClient(
                logger,
                new RecordingMessages(),
                () -> configured("not a uri", "very-secret-token"),
                new DomyaPayloadFactory(),
                "test"
        );

        try {
            Assert.assertEquals(
                    LinkResult.Status.NOT_CONFIGURED,
                    unconfiguredClient.linkPlayer(new PlayerLinkRequest("c", "u", "n", "d")).join().getStatus()
            );
            Assert.assertEquals(
                    LinkResult.Status.CONNECTION_ERROR,
                    invalidUriClient.linkPlayer(new PlayerLinkRequest("c", "u", "n", "d")).join().getStatus()
            );
        } finally {
            unconfiguredClient.close();
            invalidUriClient.close();
        }
    }

    private SyncConfig configured(String linkUrl, String token) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("api-url", linkUrl);
        yaml.set("link-url", linkUrl);
        yaml.set("secret-token", token);
        yaml.set("debug", true);
        return SyncConfig.from(yaml);
    }

    private static final class RecordingMessages implements MessageProvider {

        @Override
        public String get(CommandSender sender, String key, String... replacements) {
            return get(key, replacements);
        }

        @Override
        public String get(String key, String... replacements) {
            return key + " " + String.join(" ", replacements);
        }
    }

    private static final class RecordingHandler extends Handler {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
