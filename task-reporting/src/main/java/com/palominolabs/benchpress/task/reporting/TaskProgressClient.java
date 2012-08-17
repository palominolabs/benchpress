package com.palominolabs.benchpress.task.reporting;

import org.joda.time.Duration;

import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;

// TODO this should be more flexible for reporting task-impl-specific types of progress (e.g. latency)
@ThreadSafe
public interface TaskProgressClient {

    /**
     * @param jobId             job
     * @param partitionId       partition
     * @param reportSequenceNum a sequence num that should be incremented every time this is called, scoped per task
     *                          (allows us to detect dropped reports)
     * @param duration          duration that the work described in this report took
     * @param numQuanta         how many quanta were operated on
     */
    void reportProgress(UUID jobId, int partitionId, int reportSequenceNum, Duration duration,
        int numQuanta);

    /**
     * Indicates that all threads for the task have completed
     *
     * @param jobId             job
     * @param partitionId       partition
     * @param reportSequenceNum the last sequence num that will be used for this job/partition combo
     */
    void reportFinished(UUID jobId, int partitionId, int reportSequenceNum);
}
