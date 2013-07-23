package com.palominolabs.benchpress.worker;

import org.skife.config.Config;
import org.skife.config.Default;

public interface MetricsReporterConfig {

    //
    // Common
    //

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

    //
    // For Ganglia only
    //

    @Config("benchpress.metrics.reporter.connection-mode")
    @Default("UNICAST") // EC2 does not support multicast
    public String getConnectionMode();

}
