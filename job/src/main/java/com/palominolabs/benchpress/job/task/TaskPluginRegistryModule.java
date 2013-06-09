package com.palominolabs.benchpress.job.task;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class TaskPluginRegistryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskPluginRegistry.class);
        // provide a (possibly empty) set of bindings
        Multibinder.newSetBinder(binder(), TaskPlugin.class);
    }
}
