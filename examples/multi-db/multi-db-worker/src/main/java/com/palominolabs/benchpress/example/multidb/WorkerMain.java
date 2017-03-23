package com.palominolabs.benchpress.example.multidb;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.palominolabs.benchpress.example.multidb.cassandra.CassandraModule;
import com.palominolabs.benchpress.example.multidb.hbase.HbaseModule;
import com.palominolabs.benchpress.example.multidb.hbaseasync.HbaseAsyncModule;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.example.multidb.mongodb.MongoDbModule;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;
import com.palominolabs.benchpress.curator.CuratorModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerConnectorConfig;
import com.palominolabs.http.server.HttpServerWrapperConfig;
import com.palominolabs.http.server.HttpServerWrapperFactory;
import com.palominolabs.http.server.HttpServerWrapperModule;
import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

final class WorkerMain {
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

        injector.getInstance(WorkerMain.class).go();
    }

    private void go() throws Exception {
        curatorLifecycleHook.start();

        String host = "localhost";
        int port = 7001;
        HttpServerWrapperConfig config = new HttpServerWrapperConfig()
                .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp(host, port));

        httpServerFactory.getHttpServerWrapper(config).start();

        logger.info("Worker started listening on port " + port);

        workerAdvertiser.initListenInfo(host, port);
        workerAdvertiser.advertiseAvailability();
    }

    static class WorkerMainModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().requireExplicitBindings();
            binder().requireAtInjectOnConstructors();
            binder().requireExactBindingAnnotations();

            install(new CassandraModule());
            install(new HbaseModule());
            install(new HbaseAsyncModule());
            install(new MongoDbModule());
            install(new KeyGeneratorFactoryFactoryRegistryModule());
            install(new ValueGeneratorFactoryFactoryRegistryModule());


//            install(new TaskPluginRegistryModule());
//            install(new IpcJsonModule());
            install(new CuratorModule());
            install(new ConfigModuleBuilder().build());

            install(new HttpServerWrapperModule());
        }
    }
}
