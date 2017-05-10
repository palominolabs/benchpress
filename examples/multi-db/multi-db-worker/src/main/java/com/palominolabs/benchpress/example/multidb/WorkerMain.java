package com.palominolabs.benchpress.example.multidb;

import com.google.inject.*;
import com.palominolabs.benchpress.example.multidb.cassandra.CassandraModule;
import com.palominolabs.benchpress.example.multidb.hbase.HbaseModule;
import com.palominolabs.benchpress.example.multidb.hbaseasync.HbaseAsyncModule;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.example.multidb.mongodb.MongoDbModule;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.ipc.IpcHttpClientModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.jersey.GuiceServiceLocatorGenerator;
import com.palominolabs.benchpress.jersey.JerseySupportModule;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.task.reporting.TaskProgressClientModule;
import com.palominolabs.benchpress.worker.PartitionRunner;
import com.palominolabs.benchpress.worker.QueueProviderModule;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;
import com.palominolabs.benchpress.curator.CuratorModule;
import com.palominolabs.benchpress.worker.WorkerJerseyApp;
import com.palominolabs.benchpress.worker.http.WorkerResourceModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerConnectorConfig;
import com.palominolabs.http.server.HttpServerWrapperConfig;
import com.palominolabs.http.server.HttpServerWrapperFactory;
import com.palominolabs.http.server.HttpServerWrapperModule;
import java.util.logging.LogManager;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * A demo of building a custom main method that starts up the worker with plugins already registered.
 */
public final class WorkerMain {
    private static final Logger logger = LoggerFactory.getLogger(WorkerMain.class);

    private final HttpServerWrapperFactory httpServerFactory;
    private final WorkerAdvertiser workerAdvertiser;
    private final CuratorModule.CuratorLifecycleHook curatorLifecycleHook;

    @Inject
    WorkerMain(HttpServerWrapperFactory httpServerFactory, WorkerAdvertiser workerAdvertiser,
            CuratorModule.CuratorLifecycleHook curatorLifecycleHook) {
        this.httpServerFactory = httpServerFactory;
        this.workerAdvertiser = workerAdvertiser;
        this.curatorLifecycleHook = curatorLifecycleHook;
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new WorkerMainModule());

        JerseyGuiceUtils.install(new GuiceServiceLocatorGenerator(injector));

        injector.getInstance(WorkerMain.class).go();
    }

    private void go() throws Exception {

        String host = "localhost";
        int port = 7001;
        HttpServerWrapperConfig config = new HttpServerWrapperConfig()
                .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp(host, port));

        httpServerFactory.getHttpServerWrapper(config).start();

        logger.info("Worker started listening on port " + port);
        curatorLifecycleHook.start();

        workerAdvertiser.initListenInfo(host, port);
        workerAdvertiser.advertiseAvailability();
    }

    static class WorkerMainModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().requireExplicitBindings();
            binder().requireAtInjectOnConstructors();
            binder().requireExactBindingAnnotations();

            bind(WorkerMain.class);

            install(new CassandraModule());
            install(new HbaseModule());
            install(new HbaseAsyncModule());
            install(new MongoDbModule());
            install(new KeyGeneratorFactoryFactoryRegistryModule());
            install(new ValueGeneratorFactoryFactoryRegistryModule());

            install(new JerseySupportModule());
            bind(WorkerJerseyApp.class);

            install(new TaskPluginRegistryModule());
            install(new TaskProgressClientModule());
            install(new IpcJsonModule());
            install(new IpcHttpClientModule());
            install(new CuratorModule());
            install(new ConfigModuleBuilder().build());
            install(new JobRegistryModule());
            install(new QueueProviderModule());

            install(new WorkerResourceModule());
            bind(PartitionRunner.class);

            install(new HttpServerWrapperModule());
        }

        @Singleton
        @Provides
        ServletContainer getServletContainer(WorkerJerseyApp app) {
            return new ServletContainer(app);
        }

    }
}
