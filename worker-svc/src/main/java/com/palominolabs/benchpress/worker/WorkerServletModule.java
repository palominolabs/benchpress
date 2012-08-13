package com.palominolabs.benchpress.worker;

import com.google.common.collect.Maps;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.yammer.metrics.web.DefaultWebappMetricsFilter;

import java.util.Map;

public class WorkerServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(DefaultWebappMetricsFilter.class).in(Scopes.SINGLETON);
        filter("/*").through(DefaultWebappMetricsFilter.class);

        install(new JerseyServletModule());

        Map<String, String> guiceContainerConfig = Maps.newHashMap();
        // pojo mapping
        guiceContainerConfig.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");

        bind(GuiceContainer.class);
        serve("/*").with(GuiceContainer.class, guiceContainerConfig);
    }
}
