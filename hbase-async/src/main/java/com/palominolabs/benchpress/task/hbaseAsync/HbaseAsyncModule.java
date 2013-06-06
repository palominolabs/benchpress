package com.palominolabs.benchpress.task.hbaseAsync;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.ComponentFactory;

public final class HbaseAsyncModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ComponentFactory.class).addBinding().to(HbaseAsyncComponentFactory.class);
    }
}
