package com.palominolabs.benchpress.job.task;

/**
 * Plugins may choose to implement this interface and use the TaskOutputQueueProvider to defer processing results of tasks.
 */
public interface TaskOutputProcessor {
    /**
     * Called once per object in the queue.
     *
     * @param output an object provided to the output queue by a task
     */
    void handleOutput(Object output);
}
