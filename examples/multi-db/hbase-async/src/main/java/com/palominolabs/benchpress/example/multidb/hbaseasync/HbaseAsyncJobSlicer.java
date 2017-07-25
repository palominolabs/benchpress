package com.palominolabs.benchpress.example.multidb.hbaseasync;

import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.example.multidb.task.JobSlicerBase;

import javax.annotation.Nonnull;

final class HbaseAsyncJobSlicer extends JobSlicerBase {
    private final HbaseAsyncConfig config;

    HbaseAsyncJobSlicer(HbaseAsyncConfig config) {
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
        return HbaseAsyncJobTypePlugin.TASK_TYPE;
    }
}
