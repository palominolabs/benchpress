package com.palominolabs.benchpress.job.task;

import com.palominolabs.benchpress.task.reporting.TaskProgressClient;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * A TaskFactory creates the runnables that actually do the work. Used to create the runnables for one individual
 * partition.
 */
@NotThreadSafe
public interface TaskFactory {

    /**
     *
     * @param jobId                  job id
     * @param partitionId            the partition of the overall job that these tasks are part of
     * @param workerId               the worker that these tasks are running in
     * @param taskProgressClient     used to report progress back to the controller
     *                               (partition-scoped)
     * @return runnables
     * @throws IOException
     */
    @Nonnull
    Collection<Runnable> getRunnables(UUID jobId, int partitionId, UUID workerId) throws IOException;

    void shutdown();
}
