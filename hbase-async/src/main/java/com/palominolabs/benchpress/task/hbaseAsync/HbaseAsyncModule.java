package com.palominolabs.benchpress.task.hbaseAsync;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;

public final class HbaseAsyncModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TaskFactoryFactory.class).addBinding().to(HbaseAsyncTaskFactoryFactory.class);
        Multibinder.newSetBinder(binder(), TaskPartitioner.class).addBinding().to(HbaseAsyncTaskFactoryFactory.class);
    }
}
