package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.details.InstanceSerializer;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.curator.InstanceSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

// TODO re-advertise if connection breaks and comes back up
@Singleton
public final class WorkerAdvertiser {
    private static final Logger logger = LoggerFactory.getLogger(WorkerAdvertiser.class);

    private final CuratorFramework curatorFramework;
    private final ZookeeperConfig zookeeperConfig;
    private final InstanceSerializer<WorkerMetadata> jacksonInstanceSerializer;
    private final UUID workerId = UUID.randomUUID();

    private String listenAddress;
    private int listenPort;

    @Inject
    WorkerAdvertiser(CuratorFramework curatorFramework, ZookeeperConfig zookeeperConfig,
        InstanceSerializerFactory instanceSerializerFactory) {
        this.curatorFramework = curatorFramework;
        this.zookeeperConfig = zookeeperConfig;
        this.jacksonInstanceSerializer =
            instanceSerializerFactory.getInstanceSerializer(new TypeReference<ServiceInstance<WorkerMetadata>>() {});
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public void advertiseAvailability() {
        logger.info("Advertising availability at <" + listenAddress + ":" + listenPort + ">");

        try {
            ServiceDiscovery<WorkerMetadata> discovery = getDiscovery();
            discovery.start();
            discovery.registerService(getInstance());
            discovery.close();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void deAdvertiseAvailability() {
        logger.info("Deadvertising availability at <" + listenAddress + ":" + listenPort + ">");

        try {
            ServiceDiscovery<WorkerMetadata> discovery = getDiscovery();
            discovery.start();
            discovery.unregisterService(getInstance());
            discovery.close();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private ServiceInstance<WorkerMetadata> getInstance() throws Exception {
        WorkerMetadata workerMetadata = new WorkerMetadata(workerId, listenAddress, listenPort);
        return ServiceInstance.<WorkerMetadata>builder()
            .name(zookeeperConfig.getWorkerServiceName())
            .address(listenAddress)
            .port(listenPort)
            .id(workerId.toString())
            .payload(workerMetadata)
            .build();
    }

    private ServiceDiscovery<WorkerMetadata> getDiscovery() {
        return ServiceDiscoveryBuilder.builder(WorkerMetadata.class)
            .basePath(zookeeperConfig.getBasePath())
            .client(curatorFramework)
            .serializer(jacksonInstanceSerializer)
            .build();
    }
}
