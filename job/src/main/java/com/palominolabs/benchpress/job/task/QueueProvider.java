package com.palominolabs.benchpress.job.task;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * TaskFactory implementations may optionally use the provided queue to defer processing implementation-specific data.
 * This data is processed by another thread, the specifics of which are defined by the implementation of QueueProvider.
 * The data processing is done by whatever the appropriate ComponentFactory returns for {@link
 * ComponentFactory#getTaskOutputProcessor()}.
 */
@ThreadSafe
public interface QueueProvider {
    /**
     * Get a queue to be used for one runnable. Do not reuse the result of this across task runnables.
     *
     * @param taskType the task type
     * @param jobId    job id
     * @return A queue
     */
    @Nonnull
    BlockingQueue<Object> getQueue(String taskType, UUID jobId);

    /**
     * Should be called when the node is done with its partition for a given job.
     *
     * @param jobId job id
     */
    void removeJob(UUID jobId);
}
