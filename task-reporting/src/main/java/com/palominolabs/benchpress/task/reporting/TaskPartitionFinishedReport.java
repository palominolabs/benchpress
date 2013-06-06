package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Duration;

public final class TaskPartitionFinishedReport extends AbstractTaskReport {
    @JsonCreator
    TaskPartitionFinishedReport(@JsonProperty("partitionId") int partitionId,
                                @JsonProperty("duration") Duration duration) {
        super(partitionId, duration);
    }
}
