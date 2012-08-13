package com.palominolabs.benchpress.job.task;

import com.palominolabs.benchpress.job.id.Id;
import org.apache.commons.configuration.Configuration;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Implementations MUST be annotated with {@link Id} to be usable from a json job spec.
 */
@ThreadSafe
public interface TaskFactoryFactory {
    // TODO provide a TypeReference to a impl-specific configuration bean and pass that in instead of a Configuration
    // TODO handle job partitioning

    /**
     * @param c job configuration
     * @return a configured task factory
     */
    TaskFactory getTaskFactory(Configuration c);
}
