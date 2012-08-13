package com.palominolabs.benchpress.task;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

public final class TaskFactoryFactoryRegistryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskFactoryFactoryRegistry.class);
        // provide a (possibly empty) set of bindings
        Multibinder.newSetBinder(binder(), TaskFactoryFactory.class);
    }
}
