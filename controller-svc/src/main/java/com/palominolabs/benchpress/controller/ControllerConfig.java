package com.palominolabs.benchpress.controller;

import org.skife.config.Config;
import org.skife.config.Default;

public interface ControllerConfig {

    @Config("benchpress.controller.http-server.ip")
    @Default("127.0.0.1")
    String getHttpServerIp();

    @Config("benchpress.controller.http-server.port")
    @Default("7000")
    int getHttpServerPort();
}
