package com.palominolabs.benchpress.example.multidb.hbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.ControllerComponentFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.task.TaskPlugin;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import java.io.IOException;

@Id(HbaseTaskPlugin.TASK_TYPE)
final class HbaseTaskPlugin implements TaskPlugin {

    static final String TASK_TYPE = "HBASE";
    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    @Inject
    HbaseTaskPlugin(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return new HbaseComponentFactory(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry,
            getConfig(objectReader, configNode));
    }

    @Nonnull
    @Override
    public ControllerComponentFactory getControllerComponentFactory(final ObjectReader objectReader,
        final JsonNode configNode) throws IOException {
        final HBaseConfig config = getConfig(objectReader, configNode);

        return new ControllerComponentFactory() {
            @Nonnull
            @Override
            public TaskPartitioner getTaskPartitioner() {
                return new HbasePartitioner(config);
            }
        };
    }

    private HBaseConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.withType(HBaseConfig.class).readValue(configNode);
    }
}
