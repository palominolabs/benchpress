package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class KeyGen {
    @JsonProperty("type")
    public final String keyGenType;

    @JsonCreator
    public KeyGen(@JsonProperty("type") String keyGenType) {
        this.keyGenType = keyGenType;
    }

}
