package com.palominolabs.benchpress.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.json.Job;
import org.joda.time.Duration;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class JobStatus {
    @JsonProperty("job")
    private final Job job;

    @JsonProperty("partitionStatuses")
    private final SortedMap<Integer, PartitionStatus> partitions = new ConcurrentSkipListMap<Integer, PartitionStatus>();

    @JsonProperty("finalDuration")
    private Duration finalDuration;

    public JobStatus(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return this.job;
    }

    public void addPartitionStatus(PartitionStatus partitionStatus) {
        this.partitions.put(partitionStatus.getPartition().getPartitionId(), partitionStatus);
    }

    public PartitionStatus getPartitionStatus(int partitionId) {
        return this.partitions.get(partitionId);
    }

    public SortedMap<Integer, PartitionStatus> getPartitionStatuses() {
        return partitions;
    }

    public void setFinalDuration(Duration finalDuration) {
        this.finalDuration = finalDuration;
    }
}
