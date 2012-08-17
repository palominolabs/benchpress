package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

@Immutable
public final class Partition {
    private final UUID jobId;
    private final int partitionId;
    private final Task task;
    private final String progressUrl;
    private final String finishedUrl;

    @JsonCreator
    public Partition(@JsonProperty("jobId") UUID jobId, @JsonProperty("partitionId") int partitionId,
        @JsonProperty("task") Task task, @JsonProperty("progressUrl") String progressUrl,
        @JsonProperty("finishedUrl") String finishedUrl) {
        this.jobId = jobId;
        this.partitionId = partitionId;
        this.task = task;
        this.progressUrl = progressUrl;
        this.finishedUrl = finishedUrl;
    }

    @JsonProperty("jobId")
    public UUID getJobId() {
        return jobId;
    }

    @JsonProperty("partitionId")
    public int getPartitionId() {
        return partitionId;
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
