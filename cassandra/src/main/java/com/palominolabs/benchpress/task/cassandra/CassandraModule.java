package com.palominolabs.benchpress.task.cassandra;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.ComponentFactory;

public final class CassandraModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ComponentFactory.class).addBinding().to(CassandraComponentFactory.class);
    }
}
