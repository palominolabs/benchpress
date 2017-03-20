package com.palominolabs.benchpress.worker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO re-advertise if connection breaks and comes back up
@Singleton
@ThreadSafe
public final class WorkerAdvertiser {
    private static final Logger logger = LoggerFactory.getLogger(WorkerAdvertiser.class);

    private final ZookeeperConfig zookeeperConfig;

    private final UUID workerId = UUID.randomUUID();

    private final ServiceDiscovery<WorkerMetadata> serviceDiscovery;
    @GuardedBy("this")
    private String listenAddress;

    @GuardedBy("this")
    private int listenPort;
    @Inject
    WorkerAdvertiser(ZookeeperConfig zookeeperConfig, ServiceDiscovery<WorkerMetadata> serviceDiscovery) {
        this.zookeeperConfig = zookeeperConfig;
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * This must be called before other methods are used.
     *
     * @param address address this worker is listening on
     * @param port    port this worker is listening on
     */
    public synchronized void initListenInfo(@Nonnull String address, int port) {
        if (listenAddress != null) {
            throw new IllegalStateException("Already initialized");
        }
        this.listenAddress = address;
        this.listenPort = port;
    }

    public synchronized void advertiseAvailability() {
        checkInitialized();
        logger.info("Advertising availability at <" + listenAddress + ":" + listenPort + ">");

        try {
            serviceDiscovery.registerService(getServiceInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void deAdvertiseAvailability() {
        checkInitialized();
        logger.info("Deadvertising availability at <" + listenAddress + ":" + listenPort + ">");

        try {
            serviceDiscovery.unregisterService(getServiceInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UUID getWorkerId() {
        return workerId;
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

    private void checkInitialized() {
        if (listenAddress == null) {
            throw new IllegalStateException("Not initialized");
        }
    }
}
