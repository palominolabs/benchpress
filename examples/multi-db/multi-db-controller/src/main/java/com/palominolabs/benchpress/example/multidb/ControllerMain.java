package com.palominolabs.benchpress.example.multidb;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.servlet.ServletModule;
import com.palominolabs.benchpress.controller.ControllerCoreModule;
import com.palominolabs.benchpress.controller.ControllerJerseyApp;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServerModule;
import com.palominolabs.benchpress.curator.InstanceSerializerModule;
import com.palominolabs.benchpress.example.multidb.cassandra.CassandraModule;
import com.palominolabs.benchpress.example.multidb.hbase.HbaseModule;
import com.palominolabs.benchpress.example.multidb.hbaseasync.HbaseAsyncModule;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.example.multidb.mongodb.MongoDbModule;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.jersey.GuiceServiceLocatorGenerator;
import com.palominolabs.benchpress.jersey.JerseySupportModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerConnectorConfig;
import com.palominolabs.http.server.HttpServerWrapperConfig;
import com.palominolabs.http.server.HttpServerWrapperFactory;
import com.palominolabs.http.server.HttpServerWrapperModule;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.LogManager;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

final class ControllerMain {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMain.class);

    private final HttpServerWrapperFactory httpServerFactory;
    private final JobFarmer jobFarmer;
    private final ZKServer zkServer;
    private final CuratorModule.CuratorLifecycleHook curatorLifecycleHook;
    private final ExecutorService zkServerExService = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new ControllerMainModule());
        JerseyGuiceUtils.install(new GuiceServiceLocatorGenerator(injector));

        injector.getInstance(ControllerMain.class).go();
    }

    @Inject
    ControllerMain(HttpServerWrapperFactory httpServerFactory, JobFarmer jobFarmer,
            ZKServer zkServer, CuratorModule.CuratorLifecycleHook curatorLifecycleHook) {
        this.httpServerFactory = httpServerFactory;
        this.jobFarmer = jobFarmer;
        this.zkServer = zkServer;
        this.curatorLifecycleHook = curatorLifecycleHook;
    }

    void go() throws Exception {
        // Disable Zookeeper's annoying attempt to start its own out-of-date Jetty
        System.setProperty("zookeeper.admin.enableServer", "false");

        if (zkServer.isEnabled()) {
            Future<?> zkFuture = zkServerExService.submit(zkServer);
            zkServerExService.submit(new FutureWatcher(zkFuture));
        } else {
            logger.info("Not running embedded ZooKeeper server");
        }

        String host = "localhost";
        int port = 7000;
        HttpServerWrapperConfig config = new HttpServerWrapperConfig()
                .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp(host, port));

        httpServerFactory.getHttpServerWrapper(config).start();

        curatorLifecycleHook.start();

        logger.info("Controller started listening on port " + port);

        jobFarmer.setListenAddress(host);
        jobFarmer.setListenPort(port);
    }

    /**
     * Runnable that logs if a specified Future completes
     */
    private static class FutureWatcher implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(FutureWatcher.class);
        private final Future<?> future;

        /**
         * @param future future that should never complete
         */
        private FutureWatcher(Future<?> future) {
            this.future = future;
        }

        @Override
        public void run() {
            try {
                future.get();
                logger.error("Future completed normally");
            } catch (InterruptedException e) {
                logger.info("Interrupted; aborting");
            } catch (ExecutionException e) {
                logger.error("Future failed", e);
            }
        }
    }

    static class ControllerMainModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().requireExplicitBindings();
            binder().requireAtInjectOnConstructors();
            binder().requireExactBindingAnnotations();

            bind(ControllerMain.class);

            install(new CassandraModule());
            install(new HbaseModule());
            install(new HbaseAsyncModule());
            install(new MongoDbModule());
            install(new KeyGeneratorFactoryFactoryRegistryModule());
            install(new ValueGeneratorFactoryFactoryRegistryModule());

            install(new ControllerCoreModule());
            install(new ZKServerModule());
            install(new TaskPluginRegistryModule());
            install(new IpcJsonModule());
            install(new InstanceSerializerModule());
            install(new CuratorModule());
            install(new ConfigModuleBuilder().build());

            install(new JerseySupportModule());
        }

        @Singleton
        @Provides
        ServletContainer getServletContainer(ControllerJerseyApp app) {
            return new ServletContainer(app);
        }
    }
}
