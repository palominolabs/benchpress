package com.palominolabs.benchpress.example.multidb.hbase;

import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.example.multidb.task.TaskPartitionerBase;

import javax.annotation.Nonnull;

final class HbasePartitioner extends TaskPartitionerBase {
    private final HBaseConfig config;

    HbasePartitioner(HBaseConfig config) {
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
        return HbaseTaskPlugin.TASK_TYPE;
    }
}
