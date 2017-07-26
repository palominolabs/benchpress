package com.palominolabs.benchpress.job.task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Responsible for creating the various sub-components of a BenchPress plugin.
 */
public interface ComponentFactory {
    /**
     * Read config data out of the config node using the supplied ObjectReader and create a TaskFactory.
     *
     * @return a configured task factory
     */
    @Nonnull
    TaskFactory getTaskFactory();
}
