package com.palominolabs.benchpress.controller;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.controller.http.JobResource;
import com.palominolabs.benchpress.worker.WorkerControlFactory;
import com.palominolabs.benchpress.worker.WorkerFinder;

public final class ResourceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkerFinder.class);
        bind(WorkerControlFactory.class);
        bind(JobFarmer.class);
        bind(JobResource.class);
    }
}
