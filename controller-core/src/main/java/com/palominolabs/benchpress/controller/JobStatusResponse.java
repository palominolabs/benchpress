package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.controller.SliceStatusResponse;
import com.palominolabs.benchpress.job.json.Job;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public final class JobStatusResponse {
    @JsonProperty("job")
    private final Job job;

    @JsonProperty("slices")
    private final List<SliceStatusResponse> slices;

    @JsonProperty("finished")
    private final boolean finished;

    @Nullable
    @JsonProperty("finalDuration")
    private final Duration finalDuration;

    public JobStatusResponse(Job job, List<SliceStatusResponse> slices, boolean finished,
            @Nullable Duration finalDuration) {
        this.job = job;
        this.slices = slices;
        this.finished = finished;
        this.finalDuration = finalDuration;
    }
}
