package com.palominolabs.benchpress.worker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.http.server.HttpServer;
import com.palominolabs.http.server.HttpServerConfig;
import com.palominolabs.http.server.HttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

import static com.palominolabs.benchpress.guice.ReflectiveModuleInstantiationModule.getModuleForModuleNamesString;

final class WorkerMain {
    private static final Logger logger = LoggerFactory.getLogger(WorkerMain.class);

    private final HttpServerFactory httpServerFactory;
    private final WorkerAdvertiser workerAdvertiser;
    private final WorkerConfig workerConfig;
    private final CuratorModule.CuratorLifecycleHook curatorLifecycleHook;
    private final MetricsReporter metricsReporter;

    @Inject
    WorkerMain(HttpServerFactory httpServerFactory, WorkerAdvertiser workerAdvertiser, WorkerConfig workerConfig,
        CuratorModule.CuratorLifecycleHook curatorLifecycleHook, MetricsReporter metricsReporter) {
        this.httpServerFactory = httpServerFactory;
        this.workerAdvertiser = workerAdvertiser;
        this.workerConfig = workerConfig;
        this.curatorLifecycleHook = curatorLifecycleHook;
        this.metricsReporter = metricsReporter;
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new WorkerMainModule(),
            getModuleForModuleNamesString(System.getProperty("benchpress.plugin.module-names")));

        injector.getInstance(WorkerMain.class).go();
    }

    void go() throws Exception {
        curatorLifecycleHook.start();

        metricsReporter.start();

        HttpServerConfig config = new HttpServerConfig();
        config.setHttpListenHost(workerConfig.getHttpServerIp());
        config.setHttpListenPort(workerConfig.getHttpServerPort());
        HttpServer httpServer = httpServerFactory.getHttpServer(config);
        httpServer.start();
        logger.info("Worker started listening on port " + httpServer.getHttpListenPort());

        workerAdvertiser.initListenInfo(httpServer.getHttpListenHost(), httpServer.getHttpListenPort());
        workerAdvertiser.advertiseAvailability();
    }
}
