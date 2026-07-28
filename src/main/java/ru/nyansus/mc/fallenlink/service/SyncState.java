package ru.nyansus.mc.fallenlink.service;

public final class SyncState implements SyncAvailability {

    private volatile boolean enabled;

    public SyncState(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
