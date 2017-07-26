package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.UUID;

/**
 * A wrapper around a progress client that knows the job and slice id.
 */
public class ScopedProgressClient {

    private final UUID jobId;

    private final int sliceId;

    private final String jobFinishedUrl;
    private final String jobProgressUrl;
    private final TaskProgressClient client;

    public ScopedProgressClient(UUID jobId, int sliceId, String jobFinishedUrl, String jobProgressUrl, TaskProgressClient client) {
        this.jobId = jobId;
        this.sliceId = sliceId;
        this.jobFinishedUrl = jobFinishedUrl;
        this.jobProgressUrl = jobProgressUrl;
        this.client = client;
    }

    public void reportFinished(Duration duration) {
        client.reportFinished(jobId, sliceId, duration, jobFinishedUrl);
    }

    public void reportProgress(JsonNode data) {
        client.reportProgress(jobId, sliceId, data, jobProgressUrl);
    }
}
