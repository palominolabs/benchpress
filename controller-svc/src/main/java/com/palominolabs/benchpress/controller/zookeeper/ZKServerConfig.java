package com.palominolabs.benchpress.controller.zookeeper;

import org.apache.zookeeper.server.ZooKeeperServer;
import org.skife.config.Config;
import org.skife.config.Default;

public interface ZKServerConfig {

    @Config("benchpress.controller.zookeeper.embedded-server.enable")
    @Default("true")
    boolean isEnabled();

    @Config("benchpress.controller.zookeeper.embedded-server.listen-host")
    @Default("0.0.0.0")
    String getListenHost();

    @Config("benchpress.controller.zookeeper.embedded-server.listen-port")
    @Default("2281")
    int getListenPort();

    @Config("benchpress.controller.zookeeper.embedded-server.tmp-dir")
    @Default("tmp/zookeeper-server")
    String getTmpDir();

    @Config("benchpress.controller.zookeeper.embedded-server.tick-time")
    @Default("" + ZooKeeperServer.DEFAULT_TICK_TIME)
    int getTickTime();

    @Config("benchpress.controller.zookeeper.embedded-server.max-client-connections")
    @Default("1000")
    int getMaxClientConnections();
}
