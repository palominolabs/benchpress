package com.palominolabs.benchpress.worker.metrics.ganglia;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.worker.metrics.MetricsReporter;
import com.palominolabs.config.ConfigModule;

public final class GangliaMetricsReporterModule extends AbstractModule {
    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), GangliaMetricsReporterConfig.class);
        bind(MetricsReporter.class).to(GangliaMetricsReporter.class);
    }
}
