package com.palominolabs.benchpress.job.registry;

import com.google.inject.AbstractModule;

public final class JobRegistryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JobRegistry.class).to(MemoryJobRegistry.class);
    }
}
