package com.palominolabs.benchpress.job.task;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Captures the necessary configuration (known at task startup time) to be able to create TaskOutputProcessor instances
 * later.
 */
@ThreadSafe
public interface TaskOutputProcessorFactory {

    /**
     * @return a configured TaskOutputProcessor
     */
    @Nonnull
    TaskOutputProcessor getTaskOutputProcessor();
}
