package com.palominolabs.benchpress.task.simplehttp;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.JobTypePlugin;

public final class SimpleHttpTaskModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), JobTypePlugin.class).addBinding().to(SimpleHttpJobTypePlugin.class);
    }
}
