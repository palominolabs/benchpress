package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import java.time.Duration;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public final class SliceStatusResponse {
    /**
     * The worker that was given this slice
     */
    private final WorkerMetadata workerMetadata;

    private final boolean finished;
    private final Duration duration;
    private final Task task;
    private final List<ProgressItem> progress;

    SliceStatusResponse(Task task, WorkerMetadata workerMetadata, boolean finished,
            Duration duration, List<ProgressItem> progress) {
        this.task = task;
        this.workerMetadata = workerMetadata;
        this.finished = finished;
        this.duration = duration;
        this.progress = progress;
    }

    @JsonProperty("worker")
    public WorkerMetadata getWorkerMetadata() {
        return workerMetadata;
    }

    @JsonProperty("finished")
    public boolean isFinished() {
        return finished;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return duration;
    }

    @JsonProperty("task")
    public Task getTask() {
        return task;
    }

    @JsonProperty("progress")
    public List<ProgressItem> getProgress() {
        return progress;
    }
}
