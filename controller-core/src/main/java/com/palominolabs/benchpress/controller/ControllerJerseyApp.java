package com.palominolabs.benchpress.controller;

import com.google.inject.Inject;
import com.palominolabs.benchpress.controller.http.JobResource;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.ipc.ObjectMapperContextResolver;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ControllerJerseyApp extends ResourceConfig {

    @Inject
    ControllerJerseyApp(@Ipc ObjectMapperContextResolver objectMapperContextResolver) {
        register(objectMapperContextResolver);

        register(JobResource.class);

        register(JacksonFeature.class);

        property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
        property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        property(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, true);
        property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);
    }
}
