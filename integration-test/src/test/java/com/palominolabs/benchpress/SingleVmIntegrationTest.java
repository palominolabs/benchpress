package com.palominolabs.benchpress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Stage;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.benchpress.config.ZookeeperConfig;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.controller.ResourceModule;
import com.palominolabs.benchpress.controller.zookeeper.ZKServer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServerConfig;
import com.palominolabs.benchpress.controller.zookeeper.ZKServerModule;
import com.palominolabs.benchpress.curator.InstanceSerializerModule;
import com.palominolabs.benchpress.http.server.DefaultJerseyServletModule;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.ipc.IpcHttpClientModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.task.TaskFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.job.task.TaskPartitionerRegistryModule;
import com.palominolabs.benchpress.task.reporting.TaskProgressClientModule;
import com.palominolabs.benchpress.task.simplehttp.SimpleHttpTaskModule;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;
import com.palominolabs.benchpress.worker.WorkerControlFactory;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServer;
import com.palominolabs.http.server.HttpServerConfig;
import com.palominolabs.http.server.HttpServerFactory;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

import static com.google.inject.Guice.createInjector;
import static com.mongodb.util.MyAsserts.assertFalse;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleVmIntegrationTest {

    @Inject
    WorkerAdvertiser workerAdvertiser;
    @Inject
    HttpServerFactory httpServerFactory;
    @Inject
    ZKServer zkServer;
    @Inject
    ZKServerConfig zkServerConfig;
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

    ExecutorService executorService;
    HttpServer httpServer;
    private HttpServerConfig httpServerConfig;

    @BeforeClass
    public static void setUpClass() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void setUp() throws Exception {

        final Map<String, Object> configMap = new HashMap<String, Object>();

        createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                install(new ConfigModuleBuilder().addConfiguration(new MapConfiguration(configMap))
                    .build());

                // basic zookeeper
                install(new ZKServerModule());
                install(new CuratorModule());

                install(new DefaultJerseyServletModule());

                // controller
                install(new InstanceSerializerModule());
                install(new IpcJsonModule());
                install(new TaskPartitionerRegistryModule());
                install(new ResourceModule());

                // worker
                install(new JobRegistryModule());
                install(new TaskProgressClientModule());
                install(new IpcHttpClientModule());
                install(new TaskFactoryFactoryRegistryModule());
                install(new com.palominolabs.benchpress.worker.http.ResourceModule());

                // custom task
                install(new SimpleHttpTaskModule());
                bind(SimpleHttpResource.class);
            }
        }).injectMembers(this);

        FileUtils.deleteDirectory(new File(zkServerConfig.getTmpDir()));

        executorService = Executors.newCachedThreadPool();
        executorService.submit(zkServer);

        httpServerConfig = new HttpServerConfig();
        httpServer = httpServerFactory.getHttpServer(this.httpServerConfig);
        httpServer.start();

        curatorLifecycleHook.start();

        jobFarmer.setListenAddress(httpServer.getHttpListenHost());
        jobFarmer.setListenPort(httpServer.getHttpListenPort());
    }

    @After
    public void tearDown() throws Exception {
        httpServer.stop();

        executorService.shutdownNow();

        serviceDiscovery.close();
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

        assertEquals(jobFarmer.getControllerId().toString(),
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

        configNode.put("url", "http://" + httpServer.getHttpListenHost() + ":" + httpServer.getHttpListenPort() +
            "/simple-http-test-endpoint");

        Job j = new Job(new Task("simple-http", configNode), null);

        // submit job

        Response response = asyncHttpClient.preparePut(
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
        for (int i = 0; i < 10; i++) {
            JsonNode node = objectReader.withType(JsonNode.class).readValue(
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
    }

    private String getUrlPrefix() {
        return "http://" + httpServer.getHttpListenHost() + ":" + httpServer.getHttpListenPort();
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
     * @throws Exception
     */
    private WorkerMetadata advertiseWorker() throws Exception {
        workerAdvertiser.initListenInfo(httpServerConfig.getHttpListenHost(), httpServerConfig.getHttpListenPort());
        workerAdvertiser.advertiseAvailability();

        Collection<ServiceInstance<WorkerMetadata>> instances =
            serviceDiscovery.queryForInstances(zookeeperConfig.getWorkerServiceName());
        assertEquals(1, instances.size());

        WorkerMetadata workerMetadata = instances.iterator().next().getPayload();

        assertEquals(workerAdvertiser.getWorkerId(), workerMetadata.getWorkerId());

        return workerMetadata;
    }
}
