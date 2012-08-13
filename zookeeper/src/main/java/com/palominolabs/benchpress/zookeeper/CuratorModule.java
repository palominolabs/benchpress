package com.palominolabs.benchpress.zookeeper;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryNTimes;
import com.netflix.curator.utils.EnsurePath;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.config.ConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public final class CuratorModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(CuratorModule.class);

    private CuratorFramework curatorFramework;

    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), ZookeeperConfig.class);
    }

    @Provides
    public CuratorFramework getCuratorFramework(ZookeeperConfig zookeeperConfig) {
        doSetup(zookeeperConfig);
        return curatorFramework;
    }

    private void doSetup(ZookeeperConfig zookeeperConfig) {
        try {
            curatorFramework = CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(1000)
                .retryPolicy(new RetryNTimes(10, 500))
                .connectString(zookeeperConfig.getConnectionString())
                .build();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        curatorFramework.start();

        // Create our base path
        try {
            new EnsurePath(zookeeperConfig.getBasePath()).ensure(curatorFramework.getZookeeperClient());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
