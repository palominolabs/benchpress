package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessor;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

@Id("simple-http")
final class SimpleHttpComponentFactory implements ComponentFactory {
    @Nonnull
    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {

        ObjectNode obj = objectReader.withType(ObjectNode.class).readValue(configNode);
        String url = obj.get("url").textValue();

        return new SimpleHttpTaskFactory(url);
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory(ObjectReader objectReader, JsonNode configNode) {
        return new TaskOutputProcessorFactory() {
            @Nonnull
            @Override
            public TaskOutputProcessor getTaskOutputProcessor() {
                return SimpleHttpTaskOutputProcessor.INSTANCE;
            }
        };
    }

    @Nonnull
    @Override
    public TaskPartitioner getTaskPartitioner() {
        return new SimpleHttpTaskPartitioner();
    }
}
