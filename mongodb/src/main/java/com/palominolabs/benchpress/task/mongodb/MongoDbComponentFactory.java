package com.palominolabs.benchpress.task.mongodb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.base.task.TaskFactoryFactoryPartitionerBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

final class MongoDbComponentFactory extends TaskFactoryFactoryPartitionerBase implements ComponentFactory {
    static final String TASK_TYPE = "MONGODB";

    @Inject
    MongoDbComponentFactory(
        KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        MongoDbConfig c = getConfig(objectReader, configNode);

        return new MongoDbTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(),
            c.getNumThreads(), c.getHostname(), c.getPort(), c.getDbName(),
            c.getCollectionName());
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory(ObjectReader objectReader, JsonNode configNode) {
        return null;
    }

    @Nonnull
    @Override
    public TaskPartitioner getTaskPartitioner() {
        return this;
    }

    @Nonnull
    @Override
    protected MongoDbConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.withType(MongoDbConfig.class).readValue(configNode);
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }
}
