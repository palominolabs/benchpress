package com.palominolabs.benchpress.task.hbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.base.task.TaskFactoryFactoryPartitionerBase;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessor;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

final class HbaseComponentFactory extends TaskFactoryFactoryPartitionerBase implements ComponentFactory, TaskPartitioner {

    static final String TASK_TYPE = "HBASE";

    @Inject
    HbaseComponentFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        HBaseConfig c = getConfig(objectReader, configNode);

        return new HbaseTaskFactory(c.getTable(), c.getZkPort(), c.getZkQuorum(), c.getColumnFamily(), c.getQualifier(),
            c.isAutoFlush(), c.getWriteBufferSize(), getValueGeneratorFactory(c), getKeyGeneratorFactory(c),
            c.getTaskOperation(), c.getNumThreads(), c.getNumQuanta(), c.getBatchSize());
    }

    @Nullable
    @Override
    public TaskOutputProcessor getTaskOutputProcessor() {
        return null;
    }

    @Nonnull
    @Override
    public TaskPartitioner getTaskPartitioner() {
        return this;
    }

    @Nonnull
    @Override
    protected HBaseConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.withType(HBaseConfig.class).readValue(configNode);
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }
}
