package ru.nyansus.mc.fallenlink.scheduler;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import ru.nyansus.mc.fallenlink.service.MainThreadExecutor;

public final class BukkitTaskScheduler implements MainThreadExecutor, DelayedTaskScheduler {

    private final Plugin plugin;
    private final Server server;

    public BukkitTaskScheduler(Plugin plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Runnable task) {
        server.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        server.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
}
