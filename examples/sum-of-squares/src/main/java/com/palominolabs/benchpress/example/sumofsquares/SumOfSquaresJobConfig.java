package com.palominolabs.benchpress.example.sumofsquares;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Config for the overall job, different from {@link SumOfSquaresWorkerConfig} to show that they need not be the same.
 */
class SumOfSquaresJobConfig {
    @JsonProperty("start")
    final int start;
    @JsonProperty("end")
    final int end;

    @JsonCreator
    SumOfSquaresJobConfig(@JsonProperty("start") int start,
            @JsonProperty("end") int end) {
        this.start = start;
        this.end = end;
    }
}
