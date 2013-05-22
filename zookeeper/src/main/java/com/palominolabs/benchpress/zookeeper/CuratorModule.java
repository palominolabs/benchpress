package com.palominolabs.benchpress.zookeeper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.curator.InstanceSerializerFactory;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import com.palominolabs.config.ConfigModule;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

/**
 * If using CuratorModule, make sure to inject CuratorLifecycleHook and run CuratorLifecycleHook.start().
 */
public final class CuratorModule extends AbstractModule {

    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), ZookeeperConfig.class);
        bind(CuratorLifecycleHook.class);
    }

    @Provides
    @Singleton
    public CuratorFramework getCuratorFramework(ZookeeperConfig zookeeperConfig) {

        return CuratorFrameworkFactory.builder()
            .connectionTimeoutMs(1000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 10))
            .connectString(zookeeperConfig.getConnectionString())
            .build();
    }

    @Provides
    @Singleton
    public ServiceDiscovery<WorkerMetadata> getServiceDiscovery(ZookeeperConfig zookeeperConfig,
        CuratorFramework curatorFramework, InstanceSerializerFactory instanceSerializerFactory) {
        return ServiceDiscoveryBuilder.builder(WorkerMetadata.class)
            .basePath(zookeeperConfig.getBasePath())
            .client(curatorFramework)
            .serializer(instanceSerializerFactory
                .getInstanceSerializer(new TypeReference<ServiceInstance<WorkerMetadata>>() {}))
            .build();
    }

    /**
     * Encapsulates Curator startup.
     */
    @Singleton
    public static class CuratorLifecycleHook {

        private final CuratorFramework curatorFramework;
        private final ServiceDiscovery<WorkerMetadata> serviceDiscovery;

        @Inject
        CuratorLifecycleHook(CuratorFramework curatorFramework, ServiceDiscovery<WorkerMetadata> serviceDiscovery) {
            this.curatorFramework = curatorFramework;
            this.serviceDiscovery = serviceDiscovery;
        }

        /**
         * Start up the curator instance and service discovery system.
         */
        public void start() throws Exception {
            curatorFramework.start();
            serviceDiscovery.start();
        }
    }
}
