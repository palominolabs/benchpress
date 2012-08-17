package com.palominolabs.benchpress.job.task;

import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.IdRegistry;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

@ThreadSafe
public final class TaskFactoryFactoryRegistry extends IdRegistry<TaskFactoryFactory> {

    @Inject
    TaskFactoryFactoryRegistry(Set<TaskFactoryFactory> taskFactoryFactories) {
        super(taskFactoryFactories);
    }
}
