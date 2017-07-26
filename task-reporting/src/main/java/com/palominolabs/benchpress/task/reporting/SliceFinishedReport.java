package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import javax.annotation.Nonnull;

public final class SliceFinishedReport extends AbstractTaskReport {

    private final Duration duration;

    @JsonCreator
    SliceFinishedReport(@JsonProperty("sliceId") int sliceId, @Nonnull @JsonProperty("duration") Duration duration) {
        super(sliceId);
        this.duration = duration;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return duration;
    }
}
