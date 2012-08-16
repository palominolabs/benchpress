package com.palominolabs.benchpress.job.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Id;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * Responsible for extracting task-type-specific configuration and creating a usable TaskFactory.
 *
 * Implementations MUST be annotated with {@link Id} to be usable from a json job spec.
 */
@ThreadSafe
public interface TaskFactoryFactory {
    /**
     * Read config data out of the config node using the supplied ObjectReader and create a TaskFactory.
     *
     * @param objectReader the ObjectReader to use to deserialize
     * @param configNode   the config node for the task
     * @return a configured task factory
     */
    TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException;
}
