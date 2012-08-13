package com.palominolabs.benchpress.config;

import org.skife.config.Config;
import org.skife.config.Default;

public interface ZookeeperConfig {
    @Config("benchpress.zookeeper.client.connection-string")
    @Default("127.0.0.1:2281")
    public String getConnectionString();

    @Config("benchpress.zookeeper.path")
    @Default("/benchpress")
    public String getBasePath();

    @Config("benchpress.zookeeper.worker.servicename")
    @Default("worker")
    public String getWorkerServiceName();
}
