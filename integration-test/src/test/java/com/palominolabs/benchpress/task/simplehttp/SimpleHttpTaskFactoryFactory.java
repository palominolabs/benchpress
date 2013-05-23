package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

@Id("simple-http")
final class SimpleHttpTaskFactoryFactory implements TaskFactoryFactory {
    @Nonnull
    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {

        ObjectNode obj = objectReader.withType(ObjectNode.class).readValue(configNode);
        String url = obj.get("url").textValue();

        return new SimpleHttpTaskFactory(url);
    }
}
