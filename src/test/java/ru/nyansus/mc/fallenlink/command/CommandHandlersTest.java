package ru.nyansus.mc.fallenlink.command;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.message.MessageProvider;
import ru.nyansus.mc.fallenlink.service.PlayerLinkUseCase;
import ru.nyansus.mc.fallenlink.service.PlayerSyncUseCase;
import ru.nyansus.mc.fallenlink.service.SyncState;
import ru.nyansus.mc.fallenlink.support.TestPlayers;

public final class CommandHandlersTest {

    @Test
    public void linkCommandValidatesSenderConfigurationAndCode() {
        KeyMessages keyMessages = new KeyMessages();
        AtomicInteger linkCalls = new AtomicInteger();
        PlayerLinkUseCase linkUseCase = (player, code) -> linkCalls.incrementAndGet();

        List<String> consoleMessages = TestPlayers.messages();
        new LinkCommand(keyMessages, () -> true, this::configured, linkUseCase)
                .onCommand(console(consoleMessages), null, "link", new String[]{"code"});
        Assert.assertEquals(List.of("command.player-only"), consoleMessages);

        List<String> invalidMessages = TestPlayers.messages();
        Player invalidPlayer = TestPlayers.player("invalid", invalidMessages);
        new LinkCommand(keyMessages, () -> true, this::configured, linkUseCase)
                .onCommand(invalidPlayer, null, "link", new String[]{"abc"});
        Assert.assertEquals(List.of("command.link-usage"), invalidMessages);

        List<String> unconfiguredMessages = TestPlayers.messages();
        Player unconfiguredPlayer = TestPlayers.player("unconfigured", unconfiguredMessages);
        new LinkCommand(
                keyMessages,
                () -> true,
                () -> SyncConfig.from(new YamlConfiguration()),
                linkUseCase
        )
                .onCommand(unconfiguredPlayer, null, "link", new String[]{"code"});
        Assert.assertEquals(List.of("command.link-not-configured"), unconfiguredMessages);

        List<String> validMessages = TestPlayers.messages();
        Player validPlayer = TestPlayers.player("valid", validMessages);
        new LinkCommand(keyMessages, () -> true, this::configured, linkUseCase)
                .onCommand(validPlayer, null, "link", new String[]{" code "});
        Assert.assertEquals(List.of("command.link-checking"), validMessages);
        Assert.assertEquals(1, linkCalls.get());
    }

    @Test
    public void pausedLinkCommandDoesNotStartLinking() {
        List<String> messages = TestPlayers.messages();
        Player player = TestPlayers.player("paused", messages);
        AtomicInteger linkCalls = new AtomicInteger();
        LinkCommand command = new LinkCommand(
                new KeyMessages(),
                () -> false,
                this::configured,
                (ignored, code) -> linkCalls.incrementAndGet()
        );

        command.onCommand(player, null, "link", new String[]{"code"});

        Assert.assertEquals(List.of("command.sync-paused"), messages);
        Assert.assertEquals(0, linkCalls.get());
    }

    @Test
    public void adminCommandRoutesStatusReloadSyncAndUsage() {
        AtomicInteger reloads = new AtomicInteger();
        AtomicInteger syncs = new AtomicInteger();
        PlayerSyncUseCase syncUseCase = new PlayerSyncUseCase() {
            @Override
            public void syncOnlinePlayers() {
                syncs.incrementAndGet();
            }

            @Override
            public void syncPlayer(Player player, boolean online) {
            }
        };
        SyncState state = new SyncState(true);
        DomyaSyncCommand command = new DomyaSyncCommand(
                new KeyMessages(),
                state,
                this::configured,
                syncUseCase,
                reloads::incrementAndGet,
                () -> state.setEnabled(false),
                () -> state.setEnabled(true),
                "1.2.3",
                () -> 7
        );
        List<String> messages = TestPlayers.messages();
        CommandSender sender = console(messages);

        command.onCommand(sender, null, "domyasync", new String[]{});
        command.onCommand(sender, null, "domyasync", new String[]{"reload"});
        command.onCommand(sender, null, "domyasync", new String[]{"sync"});
        command.onCommand(sender, null, "domyasync", new String[]{"unknown"});

        Assert.assertEquals(1, reloads.get());
        Assert.assertEquals(1, syncs.get());
        Assert.assertEquals(
                List.of(
                        "command.status-title",
                        "command.status-api",
                        "command.status-online",
                        "command.status-state",
                        "command.reload-success",
                        "command.sync-started",
                        "command.usage"
                ),
                messages
        );
    }

    @Test
    public void adminCommandPausesAndResumesIdempotently() {
        SyncState state = new SyncState(true);
        AtomicInteger syncs = new AtomicInteger();
        PlayerSyncUseCase syncUseCase = new PlayerSyncUseCase() {
            @Override
            public void syncOnlinePlayers() {
                syncs.incrementAndGet();
            }

            @Override
            public void syncPlayer(Player player, boolean online) {
            }
        };
        DomyaSyncCommand command = new DomyaSyncCommand(
                new KeyMessages(),
                state,
                this::configured,
                syncUseCase,
                () -> {
                },
                () -> state.setEnabled(false),
                () -> state.setEnabled(true),
                "1.1.0",
                () -> 0
        );
        List<String> messages = TestPlayers.messages();
        CommandSender sender = console(messages);

        command.onCommand(sender, null, "domyasync", new String[]{"pause"});
        command.onCommand(sender, null, "domyasync", new String[]{"sync"});
        command.onCommand(sender, null, "domyasync", new String[]{"pause"});
        command.onCommand(sender, null, "domyasync", new String[]{"resume"});
        command.onCommand(sender, null, "domyasync", new String[]{"resume"});
        command.onCommand(sender, null, "domyasync", new String[]{"sync"});

        Assert.assertEquals(1, syncs.get());
        Assert.assertEquals(
                List.of(
                        "command.pause-success",
                        "command.sync-paused",
                        "command.pause-already",
                        "command.resume-success",
                        "command.resume-already",
                        "command.sync-started"
                ),
                messages
        );
    }

    private SyncConfig configured() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("api-url", "https://example.test/sync");
        yaml.set("link-url", "https://example.test/link");
        yaml.set("secret-token", "valid-secret-token");
        return SyncConfig.from(yaml);
    }

    private CommandSender console(List<String> messages) {
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> {
                    if ("sendMessage".equals(method.getName()) && args != null && args.length > 0) {
                        for (Object argument : args) {
                            if (argument instanceof String) {
                                messages.add((String) argument);
                            }
                        }
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    return null;
                }
        );
    }

    private static final class KeyMessages implements MessageProvider {

        @Override
        public String get(CommandSender sender, String key, String... replacements) {
            return key;
        }

        @Override
        public String get(String key, String... replacements) {
            return key;
        }
    }
}
