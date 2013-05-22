package com.palominolabs.benchpress.worker;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

// TODO re-advertise if connection breaks and comes back up
@Singleton
public final class WorkerAdvertiser {
    private static final Logger logger = LoggerFactory.getLogger(WorkerAdvertiser.class);

    private final ZookeeperConfig zookeeperConfig;
    private final UUID workerId = UUID.randomUUID();
    private final ServiceDiscovery<WorkerMetadata> serviceDiscovery;

    private String listenAddress;
    private int listenPort;

    @Inject
    WorkerAdvertiser(ZookeeperConfig zookeeperConfig, ServiceDiscovery<WorkerMetadata> serviceDiscovery) {
        this.zookeeperConfig = zookeeperConfig;
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * @param address address this worker is listening on
     * @param port    port this worker is listening on
     */
    public void setListenInfo(String address, int port) {
        this.listenAddress = address;
        this.listenPort = port;
    }

    public void advertiseAvailability() {
        logger.info("Advertising availability at <" + listenAddress + ":" + listenPort + ">");

        try {
            serviceDiscovery.registerService(getServiceInstance());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void deAdvertiseAvailability() {
        logger.info("Deadvertising availability at <" + listenAddress + ":" + listenPort + ">");

        try {
            serviceDiscovery.unregisterService(getServiceInstance());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private ServiceInstance<WorkerMetadata> getServiceInstance() throws Exception {
        WorkerMetadata workerMetadata = new WorkerMetadata(workerId, listenAddress, listenPort);
        return ServiceInstance.<WorkerMetadata>builder()
            .name(zookeeperConfig.getWorkerServiceName())
            .address(listenAddress)
            .port(listenPort)
            .id(workerId.toString())
            .payload(workerMetadata)
            .build();
    }
}
