package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

public final class SliceFinishedReport extends AbstractTaskReport {
    @JsonCreator
    SliceFinishedReport(@JsonProperty("sliceId") int sliceId, @JsonProperty("duration") Duration duration) {
        super(sliceId, duration);
    }
}
