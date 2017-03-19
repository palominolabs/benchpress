package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

public final class TaskPartitionFinishedReport extends AbstractTaskReport {
    @JsonCreator
    TaskPartitionFinishedReport(@JsonProperty("partitionId") int partitionId,
                                @JsonProperty("duration") Duration duration) {
        super(partitionId, duration);
    }
}
