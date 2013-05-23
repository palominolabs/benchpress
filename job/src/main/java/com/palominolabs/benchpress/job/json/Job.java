package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nullable;
import java.util.UUID;

public final class Job {
    private final UUID jobId;

    private final Task task;

    @VisibleForTesting
    @JsonCreator
    public Job(@JsonProperty("task") Task task, @JsonProperty("jobId") @Nullable UUID jobId) {
        this.jobId = jobId == null ? UUID.randomUUID() : jobId;
        this.task = task;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Task getTask() {
        return task;
    }
}
