package com.palominolabs.benchpress.controller;

import com.google.inject.Inject;
import com.palominolabs.benchpress.controller.http.JobResource;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.jersey.JerseyResourceConfigBase;
import com.palominolabs.benchpress.jersey.ObjectMapperContextResolver;

public class ControllerJerseyApp extends JerseyResourceConfigBase {

    @Inject
    ControllerJerseyApp(@Ipc ObjectMapperContextResolver objectMapperContextResolver) {
        super(objectMapperContextResolver);

        register(JobResource.class);
    }
}
