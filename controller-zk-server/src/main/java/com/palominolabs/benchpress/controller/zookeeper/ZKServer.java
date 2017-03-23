package com.palominolabs.benchpress.controller.zookeeper;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

@Singleton
public final class ZKServer implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ZKServer.class);

    private final ZKServerConfig zkServerConfig;
    private final ZKServerWithShutdown server = new ZKServerWithShutdown();

    @Inject
    ZKServer(ZKServerConfig zkServerConfig) {
        this.zkServerConfig = zkServerConfig;
    }

    public boolean isEnabled() {
        return zkServerConfig.isEnabled();
    }

    @Override
    public void run() {
        if (!zkServerConfig.isEnabled()) {
            logger.warn("Got run even though disabled");
            return;
        }

        Properties zkProp = new Properties();
        zkProp.put("dataDir", zkServerConfig.getTmpDir());
        zkProp.put("clientPort", zkServerConfig.getListenPort());
        zkProp.put("clientPortAddress", zkServerConfig.getListenHost());
        zkProp.put("tickTime", zkServerConfig.getTickTime());
        zkProp.put("maxClientCnxns", zkServerConfig.getMaxClientConnections());

        QuorumPeerConfig quorumPeerConfig = new QuorumPeerConfig();

        try {
            quorumPeerConfig.parseProperties(zkProp);
        } catch (IOException e) {
            logger.warn("Couldn't parse config", e);
            throw Throwables.propagate(e);
        } catch (QuorumPeerConfig.ConfigException e) {
            logger.warn("Bad config", e);
            throw Throwables.propagate(e);
        }

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.readFrom(quorumPeerConfig);

        try {
            logger.info("Running on " + zkServerConfig.getListenHost() + ":" + zkServerConfig.getListenPort());
            server.runFromConfig(serverConfig);
        } catch (IOException e) {
            logger.warn("ZooKeeper server failed", e);
        }
    }

    @Override
    public void close() {
        server.shutdown();
    }

    /**
     * Exposes shutdown method.
     */
    private static class ZKServerWithShutdown extends ZooKeeperServerMain {
        @Override
        protected void shutdown() {
            super.shutdown();
        }
    }
}
