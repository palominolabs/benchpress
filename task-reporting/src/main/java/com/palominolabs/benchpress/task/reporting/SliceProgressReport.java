package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public final class SliceProgressReport extends AbstractTaskReport {

    private final JsonNode data;

    @JsonCreator
    SliceProgressReport(@JsonProperty("sliceId") int sliceId, @JsonProperty("data") JsonNode data) {
        super(sliceId);
        this.data = data;
    }

    @JsonProperty("data")
    public JsonNode getData() {
        return data;
    }
}
