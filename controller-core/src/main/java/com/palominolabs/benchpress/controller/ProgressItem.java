package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

class ProgressItem {
    @JsonProperty("timestamp")
    private final Instant timestamp = Instant.now();
    @JsonProperty("data")
    private final JsonNode data;

    ProgressItem(JsonNode data) {
        this.data = data;
    }
}
