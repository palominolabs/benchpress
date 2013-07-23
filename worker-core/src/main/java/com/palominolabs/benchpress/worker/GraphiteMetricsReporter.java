package com.palominolabs.benchpress.worker;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
final class GraphiteMetricsReporter implements MetricsReporter {
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetricsReporter.class);

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
            Graphite graphite = new Graphite(new InetSocketAddress(host, port));
            GraphiteReporter.forRegistry(metricRegistry)
                .convertDurationsTo(TimeUnit.valueOf(metricsReporterConfig.getDurationsTimeUnit()))
                .convertRatesTo(TimeUnit.valueOf(metricsReporterConfig.getRatesTimeUnit()))
                .build(graphite)
                .start(metricsReporterConfig.getInterval(), 
                    TimeUnit.valueOf(metricsReporterConfig.getIntervalTimeUnit()));
        } catch (Exception e) {
            logger.error("Could not start metrics reporter", e);
        }
    }

}
