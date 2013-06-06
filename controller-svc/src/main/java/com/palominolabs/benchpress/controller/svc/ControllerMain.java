package com.palominolabs.benchpress.controller.svc;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.controller.zookeeper.ZKServer;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.http.server.HttpServer;
import com.palominolabs.http.server.HttpServerConfig;
import com.palominolabs.http.server.HttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.LogManager;

import static com.palominolabs.benchpress.guice.ReflectiveModuleInstantiationModule.getModuleForModuleNamesString;

final class ControllerMain {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMain.class);

    private final HttpServerFactory httpServerFactory;
    private final JobFarmer jobFarmer;
    private final ControllerConfig controllerConfig;
    private final ZKServer zkServer;
    private final CuratorModule.CuratorLifecycleHook curatorLifecycleHook;

    private final ExecutorService zkServerExService = Executors.newCachedThreadPool();

    @Inject
    ControllerMain(HttpServerFactory httpServerFactory, JobFarmer jobFarmer, ControllerConfig controllerConfig,
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

        HttpServerConfig config = new HttpServerConfig();
        config.setHttpListenPort(controllerConfig.getHttpServerPort());
        config.setHttpListenHost(controllerConfig.getHttpServerIp());

        HttpServer httpServer = httpServerFactory.getHttpServer(config);
        httpServer.start();

        logger.info("Controller started listening on port " + httpServer.getHttpListenPort());

        jobFarmer.setListenAddress(httpServer.getHttpListenHost());
        jobFarmer.setListenPort(httpServer.getHttpListenPort());
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
}
