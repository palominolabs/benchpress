package com.palominolabs.benchpress.example.multidb.cassandra;

import com.palominolabs.benchpress.example.multidb.task.ComponentFactoryBase;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class CassandraComponentFactory extends ComponentFactoryBase {

    private final CassandraConfig config;

    CassandraComponentFactory(
        KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry, CassandraConfig config) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
        this.config = config;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        CassandraConfig c = config;

        return new CassandraTaskFactory(c.getTaskOperation(), getValueGeneratorFactory(c), c.getBatchSize(),
            getKeyGeneratorFactory(c), c.getNumQuanta(), c.getNumThreads(), c.getCluster(), c.getKeyspace(),
            c.getPort(), c.getSeeds(), c.getColumnFamily(), c.getColumn());
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory() {
        return null;
    }
}
