package ru.nyansus.mc.fallenlink.scheduler;

public interface DelayedTaskScheduler {

    void runLater(Runnable task, long delayTicks);
}
