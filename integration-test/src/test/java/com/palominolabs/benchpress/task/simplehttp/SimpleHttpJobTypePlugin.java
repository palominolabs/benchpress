package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.ControllerComponentFactory;
import com.palominolabs.benchpress.job.task.JobSlicer;
import com.palominolabs.benchpress.job.task.JobTypePlugin;
import java.io.IOException;
import javax.annotation.Nonnull;

public final class SimpleHttpJobTypePlugin implements JobTypePlugin {

    public static final String TASK_TYPE = "simple-http";

    @Inject
    SimpleHttpJobTypePlugin() {
    }

    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        ObjectNode obj = objectReader.forType(ObjectNode.class).readValue(configNode);
        String url = obj.get("url").textValue();

        return new SimpleHttpComponentFactory(url);
    }

    @Nonnull
    @Override
    public ControllerComponentFactory getControllerComponentFactory(ObjectReader objectReader,
            JsonNode configNode) throws IOException {
        return new ControllerComponentFactory() {
            @Nonnull
            @Override
            public JobSlicer getJobSlicer() {
                return new SimpleHttpJobSlicer();
            }
        };
    }

    @Nonnull
    @Override
    public String getRegistryId() {
        return TASK_TYPE;
    }
}
