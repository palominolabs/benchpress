package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

/**
 * The part of a job that a single worker is responsible for.
 */
@Immutable
public final class JobSlice {
    private final UUID jobId;
    private final int sliceId;
    private final Task task;
    private final String progressUrl;
    private final String finishedUrl;

    @JsonCreator
    public JobSlice(@JsonProperty("jobId") UUID jobId, @JsonProperty("sliceId") int sliceId,
        @JsonProperty("task") Task task, @JsonProperty("progressUrl") String progressUrl,
        @JsonProperty("finishedUrl") String finishedUrl) {
        this.jobId = jobId;
        this.sliceId = sliceId;
        this.task = task;
        this.progressUrl = progressUrl;
        this.finishedUrl = finishedUrl;
    }

    @JsonProperty("jobId")
    public UUID getJobId() {
        return jobId;
    }

    @JsonProperty("sliceId")
    public int getSliceId() {
        return sliceId;
    }

    @JsonProperty("task")
    public Task getTask() {
        return task;
    }

    @JsonProperty("progressUrl")
    public String getProgressUrl() {
        return progressUrl;
    }

    @JsonProperty("finishedUrl")
    public String getFinishedUrl() {
        return finishedUrl;
    }
}
