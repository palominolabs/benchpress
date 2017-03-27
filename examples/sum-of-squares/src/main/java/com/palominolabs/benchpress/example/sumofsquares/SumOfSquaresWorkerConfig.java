package com.palominolabs.benchpress.example.sumofsquares;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Config for an individual worker
 */
class SumOfSquaresWorkerConfig {
    @JsonProperty("start")
    final int first;
    @JsonProperty("end")
    final int last;

    @JsonCreator
    SumOfSquaresWorkerConfig(@JsonProperty("start") int first,
            @JsonProperty("end") int last) {
        this.first = first;
        this.last = last;
    }
}
