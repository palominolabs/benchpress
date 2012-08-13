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

import java.io.IOException;

/**
 * If using CuratorModule, make sure to inject CuratorLifecycleHook and run CuratorLifecycleHook.start().
 */
public final class CuratorModule extends AbstractModule {

    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), ZookeeperConfig.class);
    }

    @Provides
    @Singleton
    public CuratorFramework getCuratorFramework(ZookeeperConfig zookeeperConfig) {

        try {
            return CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(1000)
                .retryPolicy(new RetryNTimes(10, 500))
                .connectString(zookeeperConfig.getConnectionString())
                .build();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Provides
    @Singleton
    CuratorLifecycleHook getCuratorLifecycleHook(CuratorFramework curatorFramework, ZookeeperConfig zookeeperConfig) {
        return new CuratorLifecycleHook(curatorFramework, zookeeperConfig);
    }

    /**
     * Encapsulates Curator startup.
     */
    public static class CuratorLifecycleHook {

        private final CuratorFramework curatorFramework;
        private final ZookeeperConfig zookeeperConfig;

        public CuratorLifecycleHook(CuratorFramework curatorFramework, ZookeeperConfig zookeeperConfig) {

            this.curatorFramework = curatorFramework;
            this.zookeeperConfig = zookeeperConfig;
        }

        /**
         * Start up the curator instance
         */
        public void start() {
            curatorFramework.start();

            // Create our base path
            try {
                new EnsurePath(zookeeperConfig.getBasePath()).ensure(curatorFramework.getZookeeperClient());
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
