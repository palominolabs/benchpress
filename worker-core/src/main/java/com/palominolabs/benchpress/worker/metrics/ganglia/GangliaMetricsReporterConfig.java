package com.palominolabs.benchpress.worker.metrics.ganglia;

import org.skife.config.Config;
import org.skife.config.Default;
import com.palominolabs.benchpress.worker.metrics.MetricsReporterConfig;

public interface GangliaMetricsReporterConfig extends MetricsReporterConfig {

    @Config("benchpress.metrics.reporter.connection-mode")
    @Default("UNICAST") // EC2 does not support multicast
    public String getConnectionMode();

}
