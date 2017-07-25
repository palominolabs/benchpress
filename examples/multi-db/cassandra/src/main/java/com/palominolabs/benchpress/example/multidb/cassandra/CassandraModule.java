package com.palominolabs.benchpress.example.multidb.cassandra;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.JobTypePlugin;

public final class CassandraModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), JobTypePlugin.class).addBinding().to(CassandraJobTypePlugin.class);
    }
}
