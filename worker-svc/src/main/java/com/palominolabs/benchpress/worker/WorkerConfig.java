package com.palominolabs.benchpress.worker;

import org.skife.config.Config;
import org.skife.config.Default;

public interface WorkerConfig {

    @Config("benchpress.worker.http-server.ip")
    @Default("127.0.0.1")
    String getHttpServerIp();

    @Config("benchpress.worker.http-server.port")
    @Default("8080")
    int getHttpServerPort();
}
