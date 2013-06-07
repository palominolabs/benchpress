package com.palominolabs.benchpress.job.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Id;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * The top level integration point for a new task implementation.
 *
 *
 * Implementations MUST be annotated with {@link Id} to be usable from a json job spec.
 */
public interface TaskPlugin {
    /**
     * Encapsulates the logic that reads implementation-specific config information from the task json. This way, a
     * ComponentFactory is fully ready to use and never needs to access its configuration json again.
     *
     * @param objectReader the ObjectReader to use to deserialize configNode
     * @param configNode   the config node for the task
     * @return a configured ComponentFactory
     */
    @Nonnull
    ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException;
}
