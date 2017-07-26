package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
class SliceMetadata {
    private final Task task;

    /**
     * The worker that was given this slice
     */
    private final WorkerMetadata workerMetadata;

    @Nullable
    private Duration finalDuration;

    private final List<ProgressItem> progress =new ArrayList<>();

    SliceMetadata(Task task, WorkerMetadata workerMetadata) {
        this.task = task;
        this.workerMetadata = workerMetadata;
    }

    Task getTask() {
        return task;
    }

    void markProgress(JsonNode data) {
        progress.add(new ProgressItem(data));
    }

    WorkerMetadata getWorkerMetadata() {
        return workerMetadata;
    }

    List<ProgressItem> getProgress() {
        return new ArrayList<>(progress);
    }

    boolean isFinished() {
        return finalDuration != null;
    }

    void markFinished(@Nonnull Duration duration) {
        this.finalDuration = duration;
    }

    /**
     * @return null if not finished
     */
    @Nullable
    Duration getDuration() {
        return finalDuration;
    }
}
