package com.palominolabs.benchpress.controller.svc;

import org.skife.config.Config;
import org.skife.config.Default;

public interface ControllerConfig {

    @Config("benchpress.controller.http-server.ip")
    @Default("0.0.0.0")
    String getHttpServerIp();

    @Config("benchpress.controller.http-server.port")
    @Default("7000")
    int getHttpServerPort();
}
