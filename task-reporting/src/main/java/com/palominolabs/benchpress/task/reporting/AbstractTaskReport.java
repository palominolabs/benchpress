package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

abstract class AbstractTaskReport {
    private final int sliceId;
    private final Duration duration;

    AbstractTaskReport(int sliceId, Duration duration) {
        this.sliceId = sliceId;
        this.duration = duration;
    }

    @JsonProperty("sliceId")
    public int getSliceId() {
        return sliceId;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return duration;
    }

    public String toString() {
        return "sliceId:" + sliceId + ", duration:" + duration;
    }
}
