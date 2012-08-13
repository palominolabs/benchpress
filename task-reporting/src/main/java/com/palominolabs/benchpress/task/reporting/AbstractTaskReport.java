package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract class AbstractTaskReport {
    private final int partitionId;
    private final int reportSequenceNum;

    AbstractTaskReport(
        int partitionId, int reportSequenceNum) {
        this.partitionId = partitionId;
        this.reportSequenceNum = reportSequenceNum;
    }


    @JsonProperty("partitionId")
    public int getPartitionId() {
        return partitionId;
    }

    @JsonProperty("sequenceNum")
    public int getReportSequenceNum() {
        return reportSequenceNum;
    }

    public String toString() {
        return "partitionId:" + partitionId + ", reportSequenceNum:" + reportSequenceNum;
    }
}
