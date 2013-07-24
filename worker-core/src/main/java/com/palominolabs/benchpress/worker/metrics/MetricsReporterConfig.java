package com.palominolabs.benchpress.worker.metrics;

import org.skife.config.Config;
import org.skife.config.Default;

public interface MetricsReporterConfig {

    @Config("benchpress.metrics.reporter.connection-string")
    @Default("")
    public String getConnectionString();

    @Config("benchpress.metrics.reporter.interval")
    @Default("1")
    public int getInterval();

    @Config("benchpress.metrics.reporter.interval.units")
    @Default("SECONDS")
    public String getIntervalTimeUnit();

    @Config("benchpress.metrics.reporter.durations.units")
    @Default("MILLISECONDS")
    public String getDurationsTimeUnit();

    @Config("benchpress.metrics.reporter.rates.units")
    @Default("MINUTES")
    public String getRatesTimeUnit();

}
