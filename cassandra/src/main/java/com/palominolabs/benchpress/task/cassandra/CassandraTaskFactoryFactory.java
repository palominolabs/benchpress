package com.palominolabs.benchpress.task.cassandra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.base.task.TaskFactoryFactoryPartitionerBase;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import java.io.IOException;

@Id(CassandraTaskFactoryFactory.TASK_TYPE)
final class CassandraTaskFactoryFactory extends TaskFactoryFactoryPartitionerBase implements TaskFactoryFactory, TaskPartitioner {

    static final String TASK_TYPE = "CASSANDRA";

    @Inject
    CassandraTaskFactoryFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
    }

    @Nonnull
    @Override
    protected CassandraConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.withType(CassandraConfig.class).readValue(configNode);
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }

    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        CassandraConfig c = getConfig(objectReader, configNode);

        return new CassandraTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(),
            c.getNumThreads(), c.getProgressReportInterval(), c.getCluster(), c.getKeyspace(), c.getPort(),
            c.getSeeds(), c.getColumnFamily(),
            c.getColumn());
    }
}
