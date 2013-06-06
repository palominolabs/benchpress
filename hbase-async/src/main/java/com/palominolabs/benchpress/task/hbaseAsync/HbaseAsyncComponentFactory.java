package com.palominolabs.benchpress.task.hbaseAsync;

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

final class HbaseAsyncComponentFactory extends TaskFactoryFactoryPartitionerBase implements ComponentFactory, TaskPartitioner {

    static final String TASK_TYPE = "HBASE_ASYNC";

    @Inject
    HbaseAsyncComponentFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
    }

    @Nonnull
    @Override
    protected HBaseAsyncConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.withType(HBaseAsyncConfig.class).readValue(configNode);
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        HBaseAsyncConfig c = getConfig(objectReader, configNode);

        return new HbaseAsyncTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(), c.getNumThreads(), c.getColumnFamily(), c.getZkQuorum(),
            c.getTable(), c.getQualifier());
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
}
