package com.palominolabs.benchpress.job.task;

import com.palominolabs.benchpress.task.reporting.TaskProgressClient;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A TaskFactory creates the runnables that actually do the work. Used to create the runnables for one individual
 * partition.
 */
@NotThreadSafe
public interface TaskFactory {

    /**
     * @param workerId               the worker that these tasks are running in
     * @param partitionId            the partition of the overall job that these tasks are part of
     * @param taskProgressClient     used to report progress back to the controller
     * @param jobId                  job id
     * @param reportSequenceCounter  used to provide a sequence for all reports sent back to the controller
     *                               (partition-scoped)
     * @return runnables
     * @throws IOException
     */
    Collection<Runnable> getRunnables(UUID workerId, int partitionId, TaskProgressClient taskProgressClient, UUID jobId,
        AtomicInteger reportSequenceCounter) throws IOException;

    void shutdown();
}
