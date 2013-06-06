package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LockStatus {
    @JsonProperty("locked")
    private final boolean locked;

    @JsonProperty("controllerId")
    private final String controllerId;

    @JsonCreator
    public LockStatus(@JsonProperty("locked") boolean locked, @JsonProperty("controllerId") String controllerId) {
        this.locked = locked;
        this.controllerId = controllerId;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getControllerId() {
        return controllerId;
    }
}
