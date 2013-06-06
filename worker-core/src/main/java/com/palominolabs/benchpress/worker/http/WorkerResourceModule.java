package com.palominolabs.benchpress.worker.http;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;

public final class WorkerResourceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JobResource.class);
        bind(ControlResource.class);
        bind(WorkerAdvertiser.class);
    }
}
