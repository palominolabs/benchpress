package com.palominolabs.benchpress.job.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Identifiable;
import java.io.IOException;
import javax.annotation.Nonnull;

/**
 * The top level integration point for a new task implementation.
 */
public interface TaskPlugin extends Identifiable {
    /**
     * Get the components that are used by workers.
     *
     * Encapsulates the logic that reads implementation-specific config information from the task json. This way, a
     * ComponentFactory is fully ready to use and never needs to access its configuration json again.
     *
     * @param objectReader the ObjectReader to use to deserialize configNode
     * @param configNode   the config node for the task as split up by the TaskPartitioner
     * @return a configured ComponentFactory
     */
    @Nonnull
    ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException;

    /**
     * Get the components that are used by the controller.
     *
     * @param objectReader the ObjectReader to use to deserialize configNode
     * @param configNode   the config node for the job
     * @return a configured ControllerComponentFactory
     */
    @Nonnull
    ControllerComponentFactory getControllerComponentFactory(ObjectReader objectReader, JsonNode configNode) throws
            IOException;
}
