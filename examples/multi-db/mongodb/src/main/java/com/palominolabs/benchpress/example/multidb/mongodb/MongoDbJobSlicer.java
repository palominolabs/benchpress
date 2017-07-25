package com.palominolabs.benchpress.example.multidb.mongodb;

import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.example.multidb.task.JobSlicerBase;

import javax.annotation.Nonnull;

final class MongoDbJobSlicer extends JobSlicerBase {

    private final MongoDbConfig config;

    MongoDbJobSlicer(MongoDbConfig config) {
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
        return MongoDbJobTypePlugin.TASK_TYPE;
    }
}
