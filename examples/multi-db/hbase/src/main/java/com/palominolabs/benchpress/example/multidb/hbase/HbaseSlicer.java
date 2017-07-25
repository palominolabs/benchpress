package com.palominolabs.benchpress.example.multidb.hbase;

import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.example.multidb.task.JobSlicerBase;

import javax.annotation.Nonnull;

final class HbaseSlicer extends JobSlicerBase {
    private final HBaseConfig config;

    HbaseSlicer(HBaseConfig config) {
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
        return HbaseJobTypePlugin.TASK_TYPE;
    }
}
