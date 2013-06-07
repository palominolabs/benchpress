package com.palominolabs.benchpress.task.hbaseAsync;

import com.palominolabs.benchpress.job.base.task.TaskPartitionerBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class HbaseAsyncComponentFactory extends TaskPartitionerBase implements ComponentFactory, TaskPartitioner {

    static final String TASK_TYPE = "HBASE_ASYNC";

    private final HbaseAsyncConfig config;

    HbaseAsyncComponentFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, HbaseAsyncConfig config) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.config = config;
    }

    @Nonnull
    @Override
    protected HbaseAsyncConfig getConfig() {
        return config;
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        HbaseAsyncConfig c = getConfig();

        return new HbaseAsyncTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(), c.getNumThreads(), c.getColumnFamily(), c.getZkQuorum(),
            c.getTable(), c.getQualifier());
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
}
