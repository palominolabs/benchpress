package com.palominolabs.benchpress.worker;

import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ganglia.GangliaReporter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
final class GangliaMetricsReporter implements MetricsReporter {
    private static final Logger logger = LoggerFactory.getLogger(GangliaMetricsReporter.class);

    @Inject private MetricRegistry metricRegistry;
    @Inject private MetricsReporterConfig metricsReporterConfig;

    @Override
    public void start() {
        String connectionString = metricsReporterConfig.getConnectionString();
        if (connectionString == null) {
            logger.debug("Connection string is empty, not starting metrics reporter");
            return;
        }

        // Parse the connection string
        String split[] = connectionString.split("\\:");
        if (split.length != 2) {
            logger.warn("Invalid metrics reporter connection string: \"" + connectionString + "\"");
            return;
        }
        String host = split[0];
        int port;
        try {
            port = Integer.valueOf(split[1]);
        } catch (NumberFormatException e) {
            logger.warn("Invalid metrics reporter connection string: \"" + connectionString + "\"");
            return;
        }

        // Start the Graphite metrics reporter
        try {
            UDPAddressingMode mode = UDPAddressingMode.valueOf(metricsReporterConfig.getConnectionMode());
            GMetric ganglia = new GMetric(host, port, mode, 1);
            GangliaReporter.forRegistry(metricRegistry)
                .convertDurationsTo(TimeUnit.valueOf(metricsReporterConfig.getDurationsTimeUnit()))
                .convertRatesTo(TimeUnit.valueOf(metricsReporterConfig.getRatesTimeUnit()))
                .build(ganglia)
                .start(metricsReporterConfig.getInterval(),
                    TimeUnit.valueOf(metricsReporterConfig.getIntervalTimeUnit()));
        } catch (Exception e) {
            logger.error("Could not start metrics reporter", e);
        }
    }

}
