package org.openmrs.module.disa.api.sync;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * This class is implemented as immutable so that it can be safelly shared
 * between multiple threads.
 */
public final class SyncStatus {
    private final LocalDateTime lastExecutionTime;
    private final long repeatInterval;
    private final boolean executing;
    private final LocalDateTime startedExecutionTime;
    private final float progress;

    public static SyncStatus initial() {
        return new SyncStatus(null, 0, false, null, 0);
    }

    public SyncStatus(LocalDateTime lastExecutionTime, long repeatInterval, boolean executing,
            LocalDateTime startExecutionTime, float progress) {
        this.progress = progress;
        this.lastExecutionTime = lastExecutionTime;
        this.executing = executing;
        this.startedExecutionTime = startExecutionTime;
        this.repeatInterval = repeatInterval;
    }

    public SyncStatus started() {
        return new SyncStatus(lastExecutionTime, repeatInterval, true, LocalDateTime.now(), 0);
    }

    public SyncStatus ended() {
        return new SyncStatus(LocalDateTime.now(), repeatInterval, false, startedExecutionTime, progress);
    }

    public SyncStatus withProgress(float progress) {
        return new SyncStatus(lastExecutionTime, repeatInterval, executing, startedExecutionTime, progress);
    }

    public boolean isExecuting() {
        return executing;
    }

    public float getProgress() {
        return progress;
    }

    public LocalDateTime getLastExecutionTime() {
        return lastExecutionTime;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public LocalDateTime getStartedExecutionTime() {
        return startedExecutionTime;
    }

    @Override
    public String toString() {
        return "SyncStatus [lastExecutionTime=" + lastExecutionTime + ", repeatInterval=" + repeatInterval
                + ", executing=" + executing + ", startedExecutionTime=" + startedExecutionTime + ", progress="
                + progress + "]";
    }
}
