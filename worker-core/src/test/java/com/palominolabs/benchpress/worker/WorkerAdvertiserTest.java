package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.curator.InstanceSerializerFactory;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.task.reporting.NoOpTaskProgressClient;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import com.palominolabs.benchpress.curator.CuratorModule;
import java.util.Collection;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class WorkerAdvertiserTest {
    @Inject
    private WorkerAdvertiser workerAdvertiser;
    @Inject
    private ZookeeperConfig zookeeperConfig;
    @Inject
    private CuratorFramework curatorFramework;
    @Inject
    private CuratorModule.CuratorLifecycleHook curatorLifecycleHook;

    private TestingServer testingServer;
    private ServiceDiscovery<WorkerMetadata> serviceDiscovery;

    @Before
    public void setUp() throws Exception {
        testingServer = new TestingServer();

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(new TestConfigModule(testingServer.getPort()));

                bind(WorkerAdvertiser.class);
                install(new CuratorModule());

                bind(SliceRunner.class);
                bind(TaskProgressClient.class).to(NoOpTaskProgressClient.class);

                install(new TaskPluginRegistryModule());
                install(new IpcJsonModule());
            }
        });

        injector.injectMembers(this);

        curatorLifecycleHook.start();

        // TODO make ZookeeperConfig not use config-magic so we don't duplicate ServiceDiscovery setup
        serviceDiscovery = ServiceDiscoveryBuilder.builder(WorkerMetadata.class)
            .basePath(zookeeperConfig.getBasePath())
            .client(curatorFramework)
            .serializer(injector.getInstance(InstanceSerializerFactory.class)
                .getInstanceSerializer(new TypeReference<ServiceInstance<WorkerMetadata>>() {}))
            .build();
        serviceDiscovery.start();
    }

    @After
    public void tearDown() throws Exception {
        workerAdvertiser = null;
        Closeables.close(serviceDiscovery, true);
        Closeables.close(curatorFramework, true);
        Closeables.close(testingServer, true);
    }

    @Test
    public void testAdvertiseAvailability() throws Exception {
        workerAdvertiser.initListenInfo("127.0.0.1", 12345);
        workerAdvertiser.advertiseAvailability();
        Collection<ServiceInstance<WorkerMetadata>> instances =
            serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(1, instances.size());

        WorkerMetadata workerMetadata = instances.iterator().next().getPayload();

        assertEquals(workerAdvertiser.getWorkerId(), workerMetadata.getWorkerId());

    }

    @Test
    public void testDeAdvertiseAvailability() throws Exception {
        workerAdvertiser.initListenInfo("127.0.0.1", 12345);
        workerAdvertiser.advertiseAvailability();
        Collection<ServiceInstance<WorkerMetadata>> instances =
            serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(1, instances.size());

        workerAdvertiser.deAdvertiseAvailability();
        instances = serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(0, instances.size());
    }
}
