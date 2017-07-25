package com.palominolabs.benchpress.example.multidb.cassandra;

import com.palominolabs.benchpress.example.multidb.task.JobSlicerBase;
import com.palominolabs.benchpress.job.task.JobSlicer;

import javax.annotation.Nonnull;

final class CassandraSlicer extends JobSlicerBase implements JobSlicer {

    private final CassandraConfig cassandraConfig;

    CassandraSlicer(CassandraConfig cassandraConfig) {
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
        return CassandraJobTypePlugin.TASK_TYPE;
    }
}
