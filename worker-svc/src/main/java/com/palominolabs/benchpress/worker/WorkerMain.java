package com.palominolabs.benchpress.worker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.palominolabs.benchpress.curator.CuratorModule;
import com.palominolabs.http.server.HttpServerWrapper;
import com.palominolabs.http.server.HttpServerWrapperConfig;
import com.palominolabs.http.server.HttpServerWrapperFactory;
import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.palominolabs.benchpress.guice.ReflectiveModuleInstantiationModule.getModuleForModuleNamesString;
import static com.palominolabs.http.server.HttpServerConnectorConfig.forHttp;

final class WorkerMain {
    private static final Logger logger = LoggerFactory.getLogger(WorkerMain.class);

    private final HttpServerWrapperFactory httpServerFactory;
    private final WorkerAdvertiser workerAdvertiser;
    private final WorkerConfig workerConfig;
    private final CuratorModule.CuratorLifecycleHook curatorLifecycleHook;

    @Inject
    WorkerMain(HttpServerWrapperFactory httpServerFactory, WorkerAdvertiser workerAdvertiser, WorkerConfig workerConfig,
            CuratorModule.CuratorLifecycleHook curatorLifecycleHook) {
        this.httpServerFactory = httpServerFactory;
        this.workerAdvertiser = workerAdvertiser;
        this.workerConfig = workerConfig;
        this.curatorLifecycleHook = curatorLifecycleHook;
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new WorkerMainModule(),
                getModuleForModuleNamesString(System.getProperty("benchpress.plugin.module-names")));

        injector.getInstance(WorkerMain.class).go();
    }

    private void go() throws Exception {
        curatorLifecycleHook.start();

        HttpServerWrapperConfig config = new HttpServerWrapperConfig().withHttpServerConnectorConfig(
                forHttp(workerConfig.getHttpServerIp(), workerConfig.getHttpServerPort()));
        HttpServerWrapper httpServer = httpServerFactory.getHttpServerWrapper(config);
        httpServer.start();
        logger.info("Worker started listening on port " + workerConfig.getHttpServerPort());

        workerAdvertiser.initListenInfo(workerConfig.getHttpServerIp(), workerConfig.getHttpServerPort());
        workerAdvertiser.advertiseAvailability();
    }
}
