package com.palominolabs.benchpress.task.hbaseAsync;

import com.palominolabs.benchpress.job.base.task.TaskConfigBase;
import com.palominolabs.benchpress.job.base.task.TaskPartitionerBase;

import javax.annotation.Nonnull;

final class HbaseAsyncTaskPartitioner extends TaskPartitionerBase {
    private final HbaseAsyncConfig config;

    HbaseAsyncTaskPartitioner(HbaseAsyncConfig config) {
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
        return HbaseAsyncTaskPlugin.TASK_TYPE;
    }
}
