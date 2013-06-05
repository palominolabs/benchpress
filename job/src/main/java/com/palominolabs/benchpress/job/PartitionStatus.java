package com.palominolabs.benchpress.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.json.Partition;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import org.joda.time.Duration;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public final class PartitionStatus {
    @JsonIgnore
    private final Partition partition;

    /**
     * The worker that was given this partition
     */
    @JsonIgnore
    private final WorkerMetadata workerMetadata;

    private boolean finished = false;
    private Duration duration;

    public PartitionStatus(Partition partition, WorkerMetadata workerMetadata) {
        this.partition = partition;
        this.workerMetadata = workerMetadata;
    }

    public Partition getPartition() {
        return partition;
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
