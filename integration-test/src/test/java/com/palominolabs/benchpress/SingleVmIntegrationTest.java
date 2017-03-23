package com.palominolabs.benchpress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.controller.ControllerCoreModule;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.controller.http.ControllerJobResource;
import com.palominolabs.benchpress.controller.zookeeper.ZKServer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServerModule;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.ipc.IpcHttpClientModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.jersey.GuiceServiceLocatorGenerator;
import com.palominolabs.benchpress.jersey.JerseyResourceConfigBase;
import com.palominolabs.benchpress.jersey.JerseySupportModule;
import com.palominolabs.benchpress.jersey.ObjectMapperContextResolver;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.task.reporting.TaskProgressClientModule;
import com.palominolabs.benchpress.task.simplehttp.SimpleHttpTaskModule;
import com.palominolabs.benchpress.task.simplehttp.SimpleHttpTaskOutputProcessor;
import com.palominolabs.benchpress.task.simplehttp.SimpleHttpTaskPlugin;
import com.palominolabs.benchpress.worker.PartitionRunner;
import com.palominolabs.benchpress.worker.QueueProviderModule;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;
import com.palominolabs.benchpress.worker.WorkerControlFactory;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import com.palominolabs.benchpress.worker.http.WorkerControlResource;
import com.palominolabs.benchpress.worker.http.WorkerJobResource;
import com.palominolabs.benchpress.worker.http.WorkerResourceModule;
import com.palominolabs.benchpress.curator.CuratorModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerConnectorConfig;
import com.palominolabs.http.server.HttpServerWrapper;
import com.palominolabs.http.server.HttpServerWrapperConfig;
import com.palominolabs.http.server.HttpServerWrapperFactory;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import javax.ws.rs.core.MediaType;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.eclipse.jetty.server.NetworkConnector;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.google.inject.Guice.createInjector;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SingleVmIntegrationTest {

    @Inject
    WorkerAdvertiser workerAdvertiser;
    @Inject
    HttpServerWrapperFactory httpServerFactory;
    @Inject
    ZKServer zkServer;
    @Inject
    CuratorModule.CuratorLifecycleHook curatorLifecycleHook;
    @Inject
    @Ipc
    AsyncHttpClient asyncHttpClient;
    @Inject
    ServiceDiscovery<WorkerMetadata> serviceDiscovery;
    @Inject
    ZookeeperConfig zookeeperConfig;
    @Inject
    WorkerControlFactory workerControlFactory;
    @Inject
    JobFarmer jobFarmer;
    @Ipc
    @Inject
    ObjectWriter objectWriter;
    @Ipc
    @Inject
    ObjectReader objectReader;
    @Inject
    SimpleHttpResource simpleHttpResource;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    ExecutorService executorService;
    HttpServerWrapper httpServer;
    private HttpServerWrapperConfig httpServerConfig;
    private String host;
    private int port;

    @BeforeClass
    public static void setUpClass() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void setUp() throws Exception {

        File zkTmpDir = temporaryFolder.newFolder();

        final Map<String, Object> configMap = new HashMap<>();
        configMap.put("benchpress.controller.zookeeper.embedded-server.tmp-dir", zkTmpDir.getAbsolutePath());

        Injector injector = createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                binder().requireAtInjectOnConstructors();
                binder().requireExactBindingAnnotations();

                install(new ConfigModuleBuilder().addConfiguration(new MapConfiguration(configMap))
                        .build());
                install(new JerseySupportModule());

                // basic zookeeper
                install(new ZKServerModule());
                install(new CuratorModule());

                // controller
                install(new IpcJsonModule());
                install(new ControllerCoreModule());

                // worker
                install(new JobRegistryModule());
                install(new TaskProgressClientModule());
                install(new IpcHttpClientModule());
                install(new TaskPluginRegistryModule());
                install(new WorkerResourceModule());
                install(new QueueProviderModule());
                bind(PartitionRunner.class);

                // custom task
                install(new SimpleHttpTaskModule());
                bind(SimpleHttpResource.class);
            }

            @Singleton
            @Provides
            ServletContainer getServletContainer(@Ipc ObjectMapperContextResolver objectMapperContextResolver) {
                return new ServletContainer(new IntegrationTestJerseyApp(objectMapperContextResolver));
            }
        });
        JerseyGuiceUtils.install(new GuiceServiceLocatorGenerator(injector));

        injector.injectMembers(this);

        // Disable Zookeeper's annoying attempt to start its own out-of-date Jetty
        System.setProperty("zookeeper.admin.enableServer", "false");

        executorService = Executors.newCachedThreadPool();
        executorService.submit(zkServer);
        host = "localhost";
        httpServerConfig = new HttpServerWrapperConfig()
                .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp(host, 0));
        httpServer = httpServerFactory.getHttpServerWrapper(this.httpServerConfig);
        httpServer.start();

        curatorLifecycleHook.start();

        NetworkConnector connector = (NetworkConnector) httpServer.getServer().getConnectors()[0];
        port = connector.getLocalPort();

        jobFarmer.setListenAddress("localhost");
        jobFarmer.setListenPort(port);
    }

    @After
    public void tearDown() throws Exception {
        curatorLifecycleHook.close();
        executorService.shutdownNow();
        if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
            throw new RuntimeException("Executor did not shut down");
        }

        httpServer.stop();
        asyncHttpClient.close();
    }

    @Test
    public void testWorkerLockDeAdvertises() throws Exception {
        WorkerMetadata workerMetadata = advertiseWorker();

        assertTrue(lock(workerMetadata));

        assertNoWorkersAdvertised();
    }

    @Test
    public void testWorkerUnlockReAdvertises() throws Exception {
        WorkerMetadata workerMetadata = advertiseWorker();

        lock(workerMetadata);

        assertNoWorkersAdvertised();

        assertTrue(unlock(workerMetadata));

        assertWorkerAdvertised(workerMetadata);
    }

    @Test
    public void testWorkerLockStatusShowsLockingController() throws Exception {
        WorkerMetadata workerMetadata = advertiseWorker();

        lock(workerMetadata);

        assertEquals(jobFarmer.getControllerId(),
                workerControlFactory.getWorkerControl(workerMetadata).locker());
    }

    @Test
    public void testWorkerCantBeLockedTwice() throws Exception {
        WorkerMetadata workerMetadata = advertiseWorker();

        assertTrue(lock(workerMetadata));

        // can't lock it again
        assertFalse(lock(workerMetadata));
    }

    @Test
    public void testSubmitSimpleHttpJob() throws Exception {
        advertiseWorker();

        // set up job
        ObjectNode configNode = new ObjectNode(JsonNodeFactory.instance);

        configNode.put("url", getUrlPrefix() + "/simple-http-test-endpoint");

        Job j = new Job(new Task(SimpleHttpTaskPlugin.TASK_TYPE, configNode), null);

        // submit job

        Response response = asyncHttpClient.preparePost(
                getUrlPrefix() + "/controller/job")
                .setBody(objectWriter.writeValueAsString(j))
                .addHeader("Content-Type", MediaType.APPLICATION_JSON)
                .execute(new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        return response;
                    }
                }).get();

        assertEquals(ACCEPTED.getStatusCode(), response.getStatusCode());

        // wait for job to be done

        boolean timeout = true;
        Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).getSeconds() < 5) {
            JsonNode node = objectReader.forType(JsonNode.class).readValue(
                    asyncHttpClient.prepareGet(getUrlPrefix() + "/controller/job/" + j.getJobId()).execute().get()
                            .getResponseBody());

            // we always use partition id 1, hard coded into simple http task
            ObjectNode partitionStatus = (ObjectNode) node.path("partitionStatuses").path("1");

            if (partitionStatus.get("finished").asBoolean()) {
                timeout = false;
                break;
            }
        }

        // job should have finished

        assertFalse(timeout);

        // and should have hit the resource
        assertEquals(1, simpleHttpResource.counter.get());

        // and the output processor should have gotten a single "foo"
        // noinspection rawtypes
        assertEquals(Lists.newArrayList((Object) "foo"), SimpleHttpTaskOutputProcessor.INSTANCE.getObjects());

        assertEquals(1, SimpleHttpTaskOutputProcessor.INSTANCE.getCloseCount());
    }

    private String getUrlPrefix() {
        return "http://" + host + ":" + port;
    }

    private void assertWorkerAdvertised(WorkerMetadata workerMetadata) throws Exception {
        Collection<ServiceInstance<WorkerMetadata>> instances =
                serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(1, instances.size());

        assertEquals(workerMetadata.getWorkerId(), instances.iterator().next().getPayload().getWorkerId());
    }

    /**
     * @param workerMetadata worker to lock
     * @return true if lock successful
     */
    private boolean lock(WorkerMetadata workerMetadata) {
        return workerControlFactory.getWorkerControl(workerMetadata).acquireLock(jobFarmer.getControllerId());
    }

    /**
     * @param workerMetadata worker to lock
     * @return true if unlock successful
     */
    private boolean unlock(WorkerMetadata workerMetadata) {
        return workerControlFactory.getWorkerControl(workerMetadata).releaseLock(jobFarmer.getControllerId());
    }

    private void assertNoWorkersAdvertised() throws Exception {
        Collection<ServiceInstance<WorkerMetadata>> instances =
                serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(0, instances.size());
    }

    /**
     * @return the metadata loaded from ZK
     */
    private WorkerMetadata advertiseWorker() throws Exception {
        workerAdvertiser.initListenInfo(host, port);
        workerAdvertiser.advertiseAvailability();

        Collection<ServiceInstance<WorkerMetadata>> instances =
                serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(1, instances.size());

        WorkerMetadata workerMetadata = instances.iterator().next().getPayload();

        assertEquals(workerAdvertiser.getWorkerId(), workerMetadata.getWorkerId());

        return workerMetadata;
    }

    private static class IntegrationTestJerseyApp extends JerseyResourceConfigBase {
        IntegrationTestJerseyApp(ObjectMapperContextResolver objectMapperContextResolver) {
            super(objectMapperContextResolver);

            register(ControllerJobResource.class);
            register(WorkerJobResource.class);
            register(WorkerControlResource.class);

            register(SimpleHttpResource.class);
        }
    }
}
