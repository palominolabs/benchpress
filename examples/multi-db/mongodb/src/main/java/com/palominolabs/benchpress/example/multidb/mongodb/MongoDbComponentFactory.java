package com.palominolabs.benchpress.example.multidb.mongodb;

import com.palominolabs.benchpress.example.multidb.task.ComponentFactoryBase;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;

final class MongoDbComponentFactory extends ComponentFactoryBase implements ComponentFactory {

    private final MongoDbConfig config;

    MongoDbComponentFactory(
        KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, MongoDbConfig config) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.config = config;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        return new MongoDbTaskFactory(config.getTaskOperation(), getValueGeneratorFactory(config),
            config.getBatchSize(), getKeyGeneratorFactory(config), config.getNumQuanta(), config.getNumThreads(),
            config.getHostname(), config.getPort(), config.getDbName(), config.getCollectionName());
    }
}
