package com.palominolabs.benchpress.task.hbase;

import com.palominolabs.benchpress.job.base.task.TaskPartitionerBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class HbaseComponentFactory extends TaskPartitionerBase implements ComponentFactory, TaskPartitioner {

    static final String TASK_TYPE = "HBASE";

    private final HBaseConfig config;

    HbaseComponentFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, HBaseConfig config) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.config = config;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        HBaseConfig c = getConfig();

        return new HbaseTaskFactory(c.getTable(), c.getZkPort(), c.getZkQuorum(), c.getColumnFamily(), c.getQualifier(),
            c.isAutoFlush(), c.getWriteBufferSize(), getValueGeneratorFactory(c), getKeyGeneratorFactory(c),
            c.getTaskOperation(), c.getNumThreads(), c.getNumQuanta(), c.getBatchSize());
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory() {
        return null;
    }

    @Nonnull
    @Override
    public TaskPartitioner getTaskPartitioner() {
        return this;
    }

    @Nonnull
    @Override
    protected HBaseConfig getConfig() {
        return config;
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }
}
