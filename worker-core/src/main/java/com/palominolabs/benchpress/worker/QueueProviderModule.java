package com.palominolabs.benchpress.worker;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.job.task.TaskOutputQueueProvider;

public final class QueueProviderModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskOutputQueueProvider.class).to(DefaultTaskOutputQueueProvider.class);
    }
}
