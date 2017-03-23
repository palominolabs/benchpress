package com.palominolabs.benchpress.worker.http;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;

public final class WorkerResourceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkerJobResource.class);
        bind(WorkerControlResource.class);

        bind(WorkerAdvertiser.class);
    }
}
