package com.palominolabs.benchpress.worker.metrics.graphite;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.worker.metrics.MetricsReporter;
import com.palominolabs.config.ConfigModule;

public final class GraphiteMetricsReporterModule extends AbstractModule {
    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), GraphiteMetricsReporterConfig.class);
        bind(MetricsReporter.class).to(GraphiteMetricsReporter.class);
    }
}
