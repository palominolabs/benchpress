package com.palominolabs.benchpress.worker;

import com.google.inject.AbstractModule;
import com.palominolabs.config.ConfigModule;

final class DefaultMetricsReporterModule extends AbstractModule {

    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), MetricsReporterConfig.class);
        bind(MetricsReporter.class).to(GraphiteMetricsReporter.class);
    }

}
