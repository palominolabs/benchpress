package com.palominolabs.benchpress.job.task;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class TaskPartitionerRegistryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskPartitionerRegistry.class);
        Multibinder.newSetBinder(binder(), TaskPartitioner.class);
    }
}
