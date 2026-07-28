package ru.nyansus.mc.fallenlink.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitSyncConfigProvider implements SyncConfigProvider {

    private final JavaPlugin plugin;
    private volatile SyncConfig current;

    public BukkitSyncConfigProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        this.current = SyncConfig.from(plugin.getConfig());
    }

    @Override
    public SyncConfig current() {
        return current;
    }

    public void reload() {
        plugin.reloadConfig();
        current = SyncConfig.from(plugin.getConfig());
    }

    public void updateSyncEnabled(boolean enabled) {
        plugin.getConfig().set("sync-enabled", enabled);
        plugin.saveConfig();
        current = SyncConfig.from(plugin.getConfig());
    }
}
