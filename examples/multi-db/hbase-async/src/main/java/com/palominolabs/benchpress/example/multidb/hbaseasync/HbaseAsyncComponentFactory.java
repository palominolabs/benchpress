package com.palominolabs.benchpress.example.multidb.hbaseasync;

import com.palominolabs.benchpress.example.multidb.task.ComponentFactoryBase;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;

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
}
