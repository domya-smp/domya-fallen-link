package ru.nyansus.mc.fallenlink.scheduler;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Assert;
import org.junit.Test;
import ru.nyansus.mc.fallenlink.config.SyncConfig;
import ru.nyansus.mc.fallenlink.service.PlayerSyncUseCase;

public final class SchedulerAdaptersTest {

    @Test
    public void periodicSchedulerReplacesAndCancelsTasks() {
        SchedulerRecorder recorder = new SchedulerRecorder();
        Server server = recorder.server();
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        PeriodicSyncScheduler scheduler = new PeriodicSyncScheduler(
                null,
                server,
                () -> true,
                () -> config,
                new NoOpSyncUseCase()
        );

        scheduler.close();
        scheduler.reschedule();
        scheduler.reschedule();
        scheduler.close();

        Assert.assertEquals(List.of(80L, 80L), recorder.delays);
        Assert.assertEquals(List.of(1200L, 1200L), recorder.periods);
        Assert.assertEquals(List.of(42, 42), recorder.cancelledTaskIds);
    }

    @Test
    public void periodicSchedulerDoesNotCreateTaskWhilePaused() {
        SchedulerRecorder recorder = new SchedulerRecorder();
        SyncConfig config = SyncConfig.from(new YamlConfiguration());
        PeriodicSyncScheduler scheduler = new PeriodicSyncScheduler(
                null,
                recorder.server(),
                () -> false,
                () -> config,
                new NoOpSyncUseCase()
        );

        scheduler.reschedule();

        Assert.assertTrue(recorder.periods.isEmpty());
    }

    @Test
    public void bukkitTaskSchedulerDelegatesImmediateAndDelayedTasks() {
        SchedulerRecorder recorder = new SchedulerRecorder();
        BukkitTaskScheduler scheduler = new BukkitTaskScheduler(null, recorder.server());
        AtomicInteger executions = new AtomicInteger();

        scheduler.execute(executions::incrementAndGet);
        scheduler.runLater(executions::incrementAndGet, 40L);

        Assert.assertEquals(2, executions.get());
        Assert.assertEquals(List.of(40L), recorder.delays);
    }

    private static final class NoOpSyncUseCase implements PlayerSyncUseCase {

        @Override
        public void syncOnlinePlayers() {
        }

        @Override
        public void syncPlayer(Player player, boolean online) {
        }
    }

    private static final class SchedulerRecorder {

        private final List<Long> delays = new ArrayList<>();
        private final List<Long> periods = new ArrayList<>();
        private final List<Integer> cancelledTaskIds = new ArrayList<>();

        private Server server() {
            BukkitScheduler scheduler = (BukkitScheduler) Proxy.newProxyInstance(
                    BukkitScheduler.class.getClassLoader(),
                    new Class<?>[]{BukkitScheduler.class},
                    (proxy, method, args) -> {
                        if ("runTask".equals(method.getName())) {
                            ((Runnable) args[1]).run();
                            return task();
                        }
                        if ("runTaskLater".equals(method.getName())) {
                            delays.add((Long) args[2]);
                            ((Runnable) args[1]).run();
                            return task();
                        }
                        if ("runTaskTimer".equals(method.getName())) {
                            delays.add((Long) args[2]);
                            periods.add((Long) args[3]);
                            return task();
                        }
                        if ("cancelTask".equals(method.getName())) {
                            cancelledTaskIds.add((Integer) args[0]);
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    }
            );
            return (Server) Proxy.newProxyInstance(
                    Server.class.getClassLoader(),
                    new Class<?>[]{Server.class},
                    (proxy, method, args) -> "getScheduler".equals(method.getName())
                            ? scheduler
                            : defaultValue(method.getReturnType())
            );
        }

        private BukkitTask task() {
            return (BukkitTask) Proxy.newProxyInstance(
                    BukkitTask.class.getClassLoader(),
                    new Class<?>[]{BukkitTask.class},
                    (proxy, method, args) -> "getTaskId".equals(method.getName())
                            ? 42
                            : defaultValue(method.getReturnType())
            );
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            return null;
        }
    }
}
