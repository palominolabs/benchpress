package com.palominolabs.benchpress.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.json.JobSlice;
import com.palominolabs.benchpress.worker.WorkerMetadata;

import java.time.Duration;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public final class SliceStatus {
    @JsonIgnore
    private final JobSlice jobSlice;

    /**
     * The worker that was given this slice
     */
    @JsonIgnore
    private final WorkerMetadata workerMetadata;

    private boolean finished = false;
    private Duration duration;

    public SliceStatus(JobSlice jobSlice, WorkerMetadata workerMetadata) {
        this.jobSlice = jobSlice;
        this.workerMetadata = workerMetadata;
    }

    public JobSlice getJobSlice() {
        return jobSlice;
    }

    public WorkerMetadata getWorkerMetadata() {
        return workerMetadata;
    }

    public void setFinished(Duration duration) {
        finished = true;
        this.duration = duration;
    }

    @JsonProperty("finished")
    public boolean isFinished() {
        return finished;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return duration;
    }
}
