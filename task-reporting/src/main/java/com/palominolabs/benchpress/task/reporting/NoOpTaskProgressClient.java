package com.palominolabs.benchpress.task.reporting;


import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.UUID;

/**
 * Stub impl for test use
 */
public final class NoOpTaskProgressClient implements TaskProgressClient {
    @Override
    public void reportFinished(UUID jobId, int sliceId, Duration duration) {
        // no op
    }

    @Override
    public void reportProgress(UUID jobId, int sliceId, JsonNode data) {
        // no op
    }
}
