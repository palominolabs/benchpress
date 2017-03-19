package com.palominolabs.benchpress.example.multidb.mongodb;

import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.example.multidb.task.TaskPartitionerBase;

import javax.annotation.Nonnull;

final class MongoDbTaskPartitioner extends TaskPartitionerBase {

    private final MongoDbConfig config;

    MongoDbTaskPartitioner(MongoDbConfig config) {
        this.config = config;
    }

    @Nonnull
    @Override
    protected TaskConfigBase getConfig() {
        return config;
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return MongoDbTaskPlugin.TASK_TYPE;
    }
}
