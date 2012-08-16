package com.palominolabs.benchpress.job.task;

import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.IdRegistry;

import java.util.Set;

public final class TaskPartitionerRegistry extends IdRegistry<TaskPartitioner> {
    @Inject
    TaskPartitionerRegistry(Set<TaskPartitioner> instances) {
        super(instances);
    }
}
