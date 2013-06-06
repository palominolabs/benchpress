package com.palominolabs.benchpress.job.task;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

@ThreadSafe
public interface QueueProvider {
    /**
     * Provider for creating a queue for each runnable. Do not reuse the result of this across task runnables.
     * @return A queue
     * @param taskType
     * @param jobId
     */
    @Nonnull
    BlockingQueue<?> getQueue(String taskType, UUID jobId);

    void removeJob(UUID jobId);
}
