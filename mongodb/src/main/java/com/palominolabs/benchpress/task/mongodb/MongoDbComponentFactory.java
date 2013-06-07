package com.palominolabs.benchpress.task.mongodb;

import com.palominolabs.benchpress.job.base.task.TaskPartitionerBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class MongoDbComponentFactory extends TaskPartitionerBase implements ComponentFactory {
    static final String TASK_TYPE = "MONGODB";

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
        MongoDbConfig c = getConfig();

        return new MongoDbTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(),
            c.getNumThreads(), c.getHostname(), c.getPort(), c.getDbName(),
            c.getCollectionName());
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
    protected MongoDbConfig getConfig()  {
        return config;
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }
}
