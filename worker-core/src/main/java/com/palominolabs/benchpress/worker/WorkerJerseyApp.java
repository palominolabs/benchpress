package com.palominolabs.benchpress.worker;

import com.google.inject.Inject;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.jersey.JerseyResourceConfigBase;
import com.palominolabs.benchpress.jersey.ObjectMapperContextResolver;
import com.palominolabs.benchpress.worker.http.WorkerJobResource;

public class WorkerJerseyApp extends JerseyResourceConfigBase {

    @Inject
    WorkerJerseyApp(@Ipc ObjectMapperContextResolver objectMapperContextResolver) {
        super(objectMapperContextResolver);

        register(WorkerJobResource.class);
    }
}
