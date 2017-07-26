package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract class AbstractTaskReport {
    private final int sliceId;

    AbstractTaskReport(int sliceId) {
        this.sliceId = sliceId;
    }

    @JsonProperty("sliceId")
    public int getSliceId() {
        return sliceId;
    }

}
