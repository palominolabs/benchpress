package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

@Immutable
public class LockStatus {
    @JsonProperty("locked")
    private final boolean locked;

    @JsonProperty("controllerId")
    private final UUID controllerId;

    @JsonCreator
    public LockStatus(@JsonProperty("locked") boolean locked, @JsonProperty("controllerId") UUID controllerId) {
        this.locked = locked;
        this.controllerId = controllerId;
    }

    public boolean isLocked() {
        return locked;
    }

    public UUID getControllerId() {
        return controllerId;
    }
}
