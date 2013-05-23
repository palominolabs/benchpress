package com.palominolabs.benchpress.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.json.Partition;
import com.palominolabs.benchpress.task.reporting.TaskProgressReport;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import org.joda.time.Duration;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

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

    // TODO mvoe to getter
    @JsonProperty("progressReports")
    private final SortedMap<Integer, TaskProgressReport> progressReports = new ConcurrentSkipListMap<Integer, TaskProgressReport>();

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

    public void addProgressReport(TaskProgressReport taskProgressReport) {
        progressReports.put(taskProgressReport.getReportSequenceNum(), taskProgressReport);
    }

    public Duration computeTotalDuration() {
        Duration totalDuration = new Duration(0);
        for (Integer sequenceNum : progressReports.keySet()) {
            totalDuration = totalDuration.plus(progressReports.get(sequenceNum).getDuration());
        }

        return totalDuration;
    }

    public void setFinished() {
        finished = true;
    }

    @JsonProperty("finished")
    public boolean isFinished() {
        return finished;
    }
}
