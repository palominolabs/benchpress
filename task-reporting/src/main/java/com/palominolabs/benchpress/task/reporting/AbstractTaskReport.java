package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

abstract class AbstractTaskReport {
    private final int partitionId;
    private final Duration duration;

    AbstractTaskReport(int partitionId, Duration duration) {
        this.partitionId = partitionId;
        this.duration = duration;
    }


    @JsonProperty("partitionId")
    public int getPartitionId() {
        return partitionId;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return duration;
    }

    public String toString() {
        return "partitionId:" + partitionId + ", duration:" + duration;
    }
}
