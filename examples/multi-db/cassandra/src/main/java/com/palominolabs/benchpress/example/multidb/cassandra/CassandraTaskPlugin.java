package com.palominolabs.benchpress.example.multidb.cassandra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.ControllerComponentFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.task.TaskPlugin;
import java.io.IOException;
import javax.annotation.Nonnull;

final class CassandraTaskPlugin implements TaskPlugin {
    static final String TASK_TYPE = "CASSANDRA";

    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    @Inject
    CassandraTaskPlugin(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
            ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return new CassandraComponentFactory(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry,
                getConfig(objectReader, configNode));
    }

    @Nonnull
    @Override
    public ControllerComponentFactory getControllerComponentFactory(ObjectReader objectReader,
            JsonNode configNode) throws IOException {
        final CassandraConfig cassandraConfig = getConfig(objectReader, configNode);

        return new ControllerComponentFactory() {
            @Nonnull
            @Override
            public TaskPartitioner getTaskPartitioner() {
                return new CassandraPartitioner(cassandraConfig);
            }
        };
    }

    @Nonnull
    @Override
    public String getRegistryId() {
        return TASK_TYPE;
    }

    private CassandraConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.forType(CassandraConfig.class).readValue(configNode);
    }
}
