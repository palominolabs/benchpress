package com.palominolabs.benchpress.task.cassandra;

import com.palominolabs.benchpress.job.base.task.TaskPartitionerBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class CassandraComponentFactory extends TaskPartitionerBase implements ComponentFactory, TaskPartitioner {

    static final String TASK_TYPE = "CASSANDRA";

    private final CassandraConfig cassandraConfig;

    CassandraComponentFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, CassandraConfig cassandraConfig) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.cassandraConfig = cassandraConfig;
    }

    @Nonnull
    @Override
    protected CassandraConfig getConfig() {
        return cassandraConfig;
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        CassandraConfig c = getConfig();

        return new CassandraTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(), c.getNumThreads(), c.getCluster(), c.getKeyspace(),
            c.getPort(), c.getSeeds(), c.getColumnFamily(), c.getColumn());
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
