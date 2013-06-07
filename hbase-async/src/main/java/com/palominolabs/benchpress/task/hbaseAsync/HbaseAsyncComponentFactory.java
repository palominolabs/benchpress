package com.palominolabs.benchpress.task.hbaseAsync;

import com.palominolabs.benchpress.job.base.task.ComponentFactoryBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class HbaseAsyncComponentFactory extends ComponentFactoryBase implements ComponentFactory {

    private final HbaseAsyncConfig config;

    HbaseAsyncComponentFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, HbaseAsyncConfig config) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.config = config;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        return new HbaseAsyncTaskFactory(
            config.getTaskOperation(), getValueGeneratorFactory(config), config.getBatchSize(),
            getKeyGeneratorFactory(config), config.getNumQuanta(), config.getNumThreads(), config.getColumnFamily(),
            config.getZkQuorum(), config.getTable(), config.getQualifier());
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory() {
        return null;
    }
}
