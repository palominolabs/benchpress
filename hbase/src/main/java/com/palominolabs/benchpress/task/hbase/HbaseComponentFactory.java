package com.palominolabs.benchpress.task.hbase;

import com.palominolabs.benchpress.job.base.task.ComponentFactoryBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class HbaseComponentFactory extends ComponentFactoryBase implements ComponentFactory {

    private final HBaseConfig config;

    HbaseComponentFactory(
        KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, HBaseConfig config) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.config = config;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        return new HbaseTaskFactory(
            config.getTable(), config.getZkPort(), config.getZkQuorum(), config.getColumnFamily(),
            config.getQualifier(), config.isAutoFlush(), config.getWriteBufferSize(), getValueGeneratorFactory(config),
            getKeyGeneratorFactory(config), config.getTaskOperation(), config.getNumThreads(), config.getNumQuanta(),
            config.getBatchSize());
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory() {
        return null;
    }
}
