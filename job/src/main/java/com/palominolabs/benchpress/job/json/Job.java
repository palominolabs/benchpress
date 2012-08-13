package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Job {
    private UUID jobId = UUID.randomUUID();

    @JsonProperty("task")
    private Task task;

    public Job(@JsonProperty("jobId") @Nullable UUID jobId) {
        this.jobId = jobId == null ? UUID.randomUUID() : jobId;
    }

    public UUID getJobId() {
        return jobId;
    }

    /**
     * Split this job into partitions.
     *
     * TODO: maybe put this logic somewhere other than this bean? This should be done as part of the config-bean
     * refactoring. Each job type would need to know how to parse its own config and do its own partitioning.
     *
     * @param workers The number of partitions to create
     * @param progressUrl The URL to send progress reports to
     * @param finishedUrl The URL to send finished report to
     * @return List of the partitions
     */
    public List<Partition> partition(int workers, String progressUrl, String finishedUrl) {
        List<Partition> partitions = new ArrayList<Partition>();

        int quantaPerPartition = (int) Math.ceil(task.getNumQuanta() / workers);
        for (int partitionId = 0; partitionId < workers; partitionId++) {
            Task newTask;
            if (partitionId == workers) {
                newTask = new Task(task, quantaPerPartition);
            } else {
                newTask = new Task(task, task.getNumQuanta() - quantaPerPartition * (workers - 1));
            }

            Partition partition = new Partition(jobId, partitionId, newTask, progressUrl, finishedUrl);
            partitions.add(partition);
        }

        return partitions;
    }
}
