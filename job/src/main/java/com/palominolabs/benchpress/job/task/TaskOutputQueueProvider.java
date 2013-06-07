package com.palominolabs.benchpress.job.task;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * TaskFactory implementations may optionally use the provided queue to defer processing implementation-specific data.
 * This data is processed by another thread, the specifics of which are defined by the implementation of
 * TaskOutputQueueProvider. The data processing is done by whatever the appropriate ComponentFactory returns for {@link
 * ComponentFactory#getTaskOutputProcessorFactory()}.
 */
@ThreadSafe
public interface TaskOutputQueueProvider {
    /**
     * Get a queue to be used for one runnable. Do not reuse the result of this across task runnables.
     *
     * @param jobId                      job id
     * @param taskOutputProcessorFactory factory to use to create a TaskOutputProcessor, if needed
     * @return A queue
     */
    @Nonnull
    BlockingQueue<Object> getQueue(@Nonnull UUID jobId, @Nonnull TaskOutputProcessorFactory taskOutputProcessorFactory);

    /**
     * Called when the node is done with its partition for a given job. Will call {@link TaskOutputProcessor#close()} on
     * all processors for the job.
     *
     * @param jobId job id
     */
    void removeJob(@Nonnull UUID jobId);
}
