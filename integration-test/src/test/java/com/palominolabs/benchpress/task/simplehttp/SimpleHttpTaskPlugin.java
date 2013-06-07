package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskPlugin;

import javax.annotation.Nonnull;
import java.io.IOException;

@Id("simple-http")
final class SimpleHttpTaskPlugin implements TaskPlugin {
    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        ObjectNode obj = objectReader.withType(ObjectNode.class).readValue(configNode);
        String url = obj.get("url").textValue();

        return new SimpleHttpComponentFactory(url);
    }
}
