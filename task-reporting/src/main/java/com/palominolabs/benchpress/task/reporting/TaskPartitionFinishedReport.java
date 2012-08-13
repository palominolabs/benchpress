package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TaskPartitionFinishedReport extends AbstractTaskReport {
    @JsonCreator
    TaskPartitionFinishedReport(@JsonProperty("partitionId") int partitionId, @JsonProperty("sequenceNum") int reportSequenceNum) {
        super(partitionId, reportSequenceNum);
    }
}
