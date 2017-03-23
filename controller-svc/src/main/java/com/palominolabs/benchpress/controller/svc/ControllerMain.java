package com.palominolabs.benchpress.controller.svc;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.palominolabs.benchpress.controller.ControllerCoreModule;
import com.palominolabs.benchpress.controller.ControllerJerseyApp;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServerModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.jersey.GuiceServiceLocatorGenerator;
import com.palominolabs.benchpress.jersey.JerseySupportModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.curator.CuratorModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerWrapper;
import com.palominolabs.http.server.HttpServerWrapperConfig;
import com.palominolabs.http.server.HttpServerWrapperFactory;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.LogManager;
import org.apache.commons.configuration.SystemConfiguration;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.palominolabs.benchpress.guice.ReflectiveModuleInstantiationModule.getModuleForModuleNamesString;
import static com.palominolabs.http.server.HttpServerConnectorConfig.forHttp;

final class ControllerMain {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMain.class);

    private final HttpServerWrapperFactory httpServerFactory;
    private final JobFarmer jobFarmer;
    private final ControllerConfig controllerConfig;
    private final ZKServer zkServer;
    private final CuratorModule.CuratorLifecycleHook curatorLifecycleHook;

    private final ExecutorService zkServerExService = Executors.newCachedThreadPool();

    @Inject
    ControllerMain(HttpServerWrapperFactory httpServerFactory, JobFarmer jobFarmer, ControllerConfig controllerConfig,
            ZKServer zkServer, CuratorModule.CuratorLifecycleHook curatorLifecycleHook) {
        this.httpServerFactory = httpServerFactory;
        this.jobFarmer = jobFarmer;
        this.controllerConfig = controllerConfig;
        this.zkServer = zkServer;
        this.curatorLifecycleHook = curatorLifecycleHook;
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new ControllerMainModule(),
                getModuleForModuleNamesString(System.getProperty("benchpress.plugin.module-names")));

        JerseyGuiceUtils.install(new GuiceServiceLocatorGenerator(injector));

        injector.getInstance(ControllerMain.class).go();
    }

    void go() throws Exception {
        if (zkServer.isEnabled()) {
            Future<?> zkFuture = zkServerExService.submit(zkServer);
            zkServerExService.submit(new FutureWatcher(zkFuture));
        } else {
            logger.info("Not running embedded ZooKeeper server");
        }

        curatorLifecycleHook.start();

        HttpServerWrapperConfig config = new HttpServerWrapperConfig().withHttpServerConnectorConfig(
                forHttp(controllerConfig.getHttpServerIp(), controllerConfig.getHttpServerPort()));

        HttpServerWrapper httpServer = httpServerFactory.getHttpServerWrapper(config);
        httpServer.start();

        logger.info("Controller started listening on port " + controllerConfig.getHttpServerPort());

        jobFarmer.setListenAddress(controllerConfig.getHttpServerIp());
        jobFarmer.setListenPort(controllerConfig.getHttpServerPort());
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

    public static final class ControllerMainModule extends AbstractModule {
        @Override
        protected void configure() {
            binder().requireExplicitBindings();
            binder().requireAtInjectOnConstructors();
            binder().requireExactBindingAnnotations();

            bind(ControllerMain.class);

            install(new ConfigModuleBuilder().addConfiguration(new SystemConfiguration()).build());
            ConfigModule.bindConfigBean(binder(), ControllerConfig.class);

            install(new ControllerCoreModule());
            install(new IpcJsonModule());
            install(new TaskPluginRegistryModule());
            install(new JerseySupportModule());

            install(new ZKServerModule());
            install(new CuratorModule());
        }

        @Singleton
        @Provides
        ServletContainer getServletContainer(ControllerJerseyApp app) {
            return new ServletContainer(app);
        }
    }
}
