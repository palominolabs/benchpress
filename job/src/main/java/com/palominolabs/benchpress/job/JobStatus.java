package com.palominolabs.benchpress.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.json.Job;
import java.time.Duration;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class JobStatus {
    @JsonProperty("job")
    private final Job job;

    @JsonProperty("sliceStatuses")
    private final SortedMap<Integer, SliceStatus> slices = new ConcurrentSkipListMap<>();

    @JsonProperty("fullySliced")
    private boolean fullySliced;

    @JsonProperty("finalDuration")
    private Duration finalDuration;

    public JobStatus(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return this.job;
    }

    public void addSliceStatus(SliceStatus sliceStatus) {
        this.slices.put(sliceStatus.getJobSlice().getSliceId(), sliceStatus);
    }

    public SliceStatus getSliceStatus(int sliceId) {
        return this.slices.get(sliceId);
    }

    public SortedMap<Integer, SliceStatus> getSliceStatuses() {
        return slices;
    }

    public void setFullySliced() {
        fullySliced = true;
    }

    public void setFinalDuration(Duration finalDuration) {
        this.finalDuration = finalDuration;
    }

    @JsonIgnore
    public boolean isFinished() {
        if (!fullySliced) {
            return false;
        }

        boolean finished = true;
        for (SliceStatus sliceStatus : slices.values()) {
            if (!sliceStatus.isFinished()) {
                finished = false;
                break;
            }
        }
        return finished;
    }
}
