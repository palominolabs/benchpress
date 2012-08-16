package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.test.TestingServer;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.curator.InstanceSerializerFactory;
import com.palominolabs.benchpress.curator.InstanceSerializerModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.task.TaskFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.task.reporting.NoOpTaskProgressClient;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import com.palominolabs.benchpress.worker.http.ResourceModule;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.http.server.HttpServerModule;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

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

                install(new HttpServerModule());

                bind(MetricsRegistry.class).toInstance(Metrics.defaultRegistry());

                install(new WorkerServletModule());
                install(new ResourceModule());
                install(new CuratorModule());

                install(new InstanceSerializerModule());

                bind(PartitionRunner.class);
                install(new JobRegistryModule());
                bind(TaskProgressClient.class).to(NoOpTaskProgressClient.class);

                install(new TaskFactoryFactoryRegistryModule());
                install(new ValueGeneratorFactoryFactoryRegistryModule());
                install(new KeyGeneratorFactoryFactoryRegistryModule());
                install(new IpcJsonModule());
            }
        });

        injector.injectMembers(this);

        curatorLifecycleHook.start();

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
        Closeables.closeQuietly(curatorFramework);
        Closeables.closeQuietly(serviceDiscovery);
        Closeables.closeQuietly(testingServer);
    }

    @Test
    public void testAdvertiseAvailability() throws Exception {
        workerAdvertiser.advertiseAvailability();
        Collection<ServiceInstance<WorkerMetadata>> instances =
            serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        Assert.assertTrue(instances.size() == 1);
    }

    @Test
    public void testDeAdvertiseAvailability() throws Exception {
        workerAdvertiser.advertiseAvailability();
        Collection<ServiceInstance<WorkerMetadata>> instances =
            serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        Assert.assertTrue(instances.size() == 1);

        workerAdvertiser.deAdvertiseAvailability();
        instances = serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        Assert.assertTrue(instances.size() == 0);
    }
}
