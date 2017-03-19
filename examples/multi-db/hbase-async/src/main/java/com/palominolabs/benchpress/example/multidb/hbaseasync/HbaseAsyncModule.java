package com.palominolabs.benchpress.example.multidb.hbaseasync;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.TaskPlugin;

public final class HbaseAsyncModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TaskPlugin.class).addBinding().to(HbaseAsyncTaskPlugin.class);
    }
}
