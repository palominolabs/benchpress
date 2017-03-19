package com.palominolabs.benchpress.task.reporting;


import java.time.Duration;
import java.util.UUID;

/**
 * Stub impl for test use
 */
public final class NoOpTaskProgressClient implements TaskProgressClient {
    @Override
    public void reportFinished(UUID jobId, int partitionId, Duration duration) {
        // no op
    }
}
