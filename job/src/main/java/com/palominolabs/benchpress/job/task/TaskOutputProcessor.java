package com.palominolabs.benchpress.job.task;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Plugins may choose to implement this interface and use the TaskOutputQueueProvider to defer processing results of
 * tasks.
 *
 * All interaction with a single TaskOutputProcessor instance is done from one thread.
 */
@NotThreadSafe
public interface TaskOutputProcessor extends AutoCloseable {
    /**
     * Called once per object in the queue.
     *
     * @param output an object provided to the output queue by a task
     */
    void handleOutput(Object output);

    /**
     * Called on a job's processors when the job is done.
     */
    @Override
    void close();
}
