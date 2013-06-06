package com.palominolabs.benchpress.job.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This is the main interface you need to implement to create BenchPress plugin.
 *
 * Implementations MUST be annotated with {@link com.palominolabs.benchpress.job.id.Id} to be usable from a json job
 * spec.
 */
public interface ComponentFactory {
    /**
     * Read config data out of the config node using the supplied ObjectReader and create a TaskFactory.
     *
     * @param objectReader the ObjectReader to use to deserialize
     * @param configNode   the config node for the task
     * @return a configured task factory
     */
    @Nonnull
    TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException;

    /**
     * @return Output processor to use or null if not using an output processor
     */
    @Nullable
    TaskOutputProcessor getTaskOutputProcessor();

    @Nonnull
    TaskPartitioner getTaskPartitioner();
}
