package com.palominolabs.benchpress.worker;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.job.task.QueueProvider;

public final class QueueProviderModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(QueueProvider.class).to(DefaultQueueProvider.class);
    }
}
