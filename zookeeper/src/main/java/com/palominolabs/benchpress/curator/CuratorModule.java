package com.palominolabs.benchpress.curator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import com.palominolabs.config.ConfigModule;
import java.io.IOException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;

/**
 * If using CuratorModule, make sure to inject CuratorLifecycleHook and run CuratorLifecycleHook.start().
 */
public final class CuratorModule extends AbstractModule {

    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), ZookeeperConfig.class);
        bind(CuratorLifecycleHook.class);

        ObjectMapper objectMapper = new ObjectMapper();
        bind(InstanceSerializerFactory.class)
                .toInstance(new InstanceSerializerFactory(objectMapper.reader(), objectMapper.writer()));
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

    @Provides
    @Singleton
    public ServiceProvider<WorkerMetadata> getServiceProvider(ServiceDiscovery<WorkerMetadata> serviceDiscovery,
            ZookeeperConfig zookeeperConfig) {
        return serviceDiscovery.serviceProviderBuilder()
                .serviceName(zookeeperConfig.getWorkerServiceName())
                .build();
    }

    /**
     * Encapsulates Curator startup.
     */
    @Singleton
    public static class CuratorLifecycleHook implements AutoCloseable {

        private final CuratorFramework curatorFramework;
        private final ServiceDiscovery<WorkerMetadata> serviceDiscovery;
        private final ServiceProvider<WorkerMetadata> serviceProvider;

        @Inject
        CuratorLifecycleHook(CuratorFramework curatorFramework, ServiceDiscovery<WorkerMetadata> serviceDiscovery,
                ServiceProvider<WorkerMetadata> serviceProvider) {
            this.curatorFramework = curatorFramework;
            this.serviceDiscovery = serviceDiscovery;
            this.serviceProvider = serviceProvider;
        }

        /**
         * Start up the curator instance and service discovery system.
         *
         * @throws Exception because Curator throws Exception
         */
        public void start() throws Exception {
            curatorFramework.start();
            serviceDiscovery.start();
            serviceProvider.start();
        }

        @Override
        public void close() throws IOException {
            serviceProvider.close();
            serviceDiscovery.close();
            curatorFramework.close();
        }
    }
}
