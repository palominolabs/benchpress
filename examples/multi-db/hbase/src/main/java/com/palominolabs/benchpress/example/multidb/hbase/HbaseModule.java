package com.palominolabs.benchpress.example.multidb.hbase;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.job.task.JobTypePlugin;

public final class HbaseModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), JobTypePlugin.class).addBinding().to(HbaseJobTypePlugin.class);
    }
}
