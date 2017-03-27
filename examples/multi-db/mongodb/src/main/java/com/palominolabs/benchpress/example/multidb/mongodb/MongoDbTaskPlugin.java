package com.palominolabs.benchpress.example.multidb.mongodb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.ControllerComponentFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.task.TaskPlugin;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import java.io.IOException;

final class MongoDbTaskPlugin implements TaskPlugin {
    static final String TASK_TYPE = "MONGODB";

    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    @Inject
    MongoDbTaskPlugin(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return new MongoDbComponentFactory(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry,
            objectReader.forType(MongoDbConfig.class).readValue(configNode));
    }

    @Nonnull
    @Override
    public ControllerComponentFactory getControllerComponentFactory(ObjectReader objectReader,
        JsonNode configNode) throws IOException {
        final MongoDbConfig config = getConfig(objectReader, configNode);

        return new ControllerComponentFactory() {
            @Nonnull
            @Override
            public TaskPartitioner getTaskPartitioner() {
                return new MongoDbTaskPartitioner(config);
            }
        };
    }

    @Nonnull
    @Override
    public String getRegistryId() {
        return TASK_TYPE;
    }

    private MongoDbConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.forType(MongoDbConfig.class).readValue(configNode);
    }
}
