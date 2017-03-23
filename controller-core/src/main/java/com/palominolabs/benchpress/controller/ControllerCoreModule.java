package com.palominolabs.benchpress.controller;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.controller.http.ControllerJobResource;
import com.palominolabs.benchpress.worker.WorkerControlFactory;

public final class ControllerCoreModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkerControlFactory.class);
        bind(JobFarmer.class);

        bind(ControllerJobResource.class);

        bind(ControllerJerseyApp.class);
    }
}
