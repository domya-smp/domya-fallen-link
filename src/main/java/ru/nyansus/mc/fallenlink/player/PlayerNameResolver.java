package ru.nyansus.mc.fallenlink.player;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.message.Messages;

public final class PlayerNameResolver {

    private final Server server;
    private final Messages messages;
    private boolean placeholderWarningLogged;

    public PlayerNameResolver(Server server, Messages messages) {
        this.server = server;
        this.messages = messages;
    }

    public String resolve(Player player, SyncConfig config) {
        if (!config.isUsePlaceholderApiName() || config.getNamePlaceholder().isEmpty()) {
            return player.getName();
        }
        if (server.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logPlaceholderWarningOnce();
            return player.getName();
        }

        String resolved = me.clip.placeholderapi.PlaceholderAPI
                .setPlaceholders(player, config.getNamePlaceholder())
                .trim();
        if (resolved.isEmpty() || resolved.equals(config.getNamePlaceholder())) {
            return player.getName();
        }
        return resolved;
    }

    private void logPlaceholderWarningOnce() {
        if (placeholderWarningLogged) {
            return;
        }
        placeholderWarningLogged = true;
        server.getLogger().warning(messages.get("log.placeholderapi-missing"));
    }
}
