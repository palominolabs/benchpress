package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.curator.InstanceSerializerFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.Collection;

public final class WorkerFinder {
    private final ZookeeperConfig zookeeperConfig;
    private final ServiceDiscovery<WorkerMetadata> discovery;

    @Inject
    WorkerFinder(CuratorFramework curatorFramework, ZookeeperConfig zookeeperConfig,
                 InstanceSerializerFactory instanceSerializerFactory) {
        this.zookeeperConfig = zookeeperConfig;

        discovery = ServiceDiscoveryBuilder.builder(WorkerMetadata.class)
            .basePath(zookeeperConfig.getBasePath())
            .client(curatorFramework)
            .serializer(instanceSerializerFactory
                .getInstanceSerializer(new TypeReference<ServiceInstance<WorkerMetadata>>() {}))
            .build();

        try {
            discovery.start();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Collection<ServiceInstance<WorkerMetadata>> getWorkers() {
        Collection<ServiceInstance<WorkerMetadata>> instances;

        try {
            instances = discovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return instances;
    }
}
