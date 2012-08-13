package com.palominolabs.benchpress.task;

import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.IdRegistry;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

@ThreadSafe
public final class TaskFactoryFactoryRegistry extends IdRegistry<TaskFactoryFactory> {

    @Inject
    TaskFactoryFactoryRegistry(Set<TaskFactoryFactory> taskFactoryFactories) {
        super(taskFactoryFactories);
    }
}
