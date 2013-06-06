package com.palominolabs.benchpress.job.task;

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
     * @param jobId         job id
     * @param partitionId   the partition of the overall job that these tasks are part of
     * @param workerId      the worker that these tasks are running in
     * @param queueProvider used to get a queue to feed task output into for later processing by the provided
     *                      TaskOutputProcessor; use is optional
     * @return runnables
     * @throws IOException
     */
    @Nonnull
    Collection<Runnable> getRunnables(UUID jobId, int partitionId, UUID workerId, QueueProvider queueProvider) throws
        IOException;

    void shutdown();
}
