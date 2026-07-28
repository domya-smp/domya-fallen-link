package ru.nyansus.mc.fallenlink.service;

public interface MainThreadExecutor {

    void execute(Runnable task);
}
