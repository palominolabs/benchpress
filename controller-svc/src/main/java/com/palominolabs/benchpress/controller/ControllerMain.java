package com.palominolabs.benchpress.controller;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.palominolabs.http.server.HttpServer;
import com.palominolabs.http.server.HttpServerConfig;
import com.palominolabs.http.server.HttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

final class ControllerMain {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMain.class);

    private final HttpServerFactory httpServerFactory;
    private final JobFarmer jobFarmer;
    private final ControllerConfig controllerConfig;

    @Inject
    ControllerMain(HttpServerFactory httpServerFactory, JobFarmer jobFarmer, ControllerConfig controllerConfig) {
        this.httpServerFactory = httpServerFactory;
        this.jobFarmer = jobFarmer;
        this.controllerConfig = controllerConfig;
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new ControllerMainModule());

        injector.getInstance(ControllerMain.class).go();
    }

    void go() throws Exception {
        HttpServerConfig config = new HttpServerConfig();
        config.setHttpListenPort(controllerConfig.getHttpServerPort());
        config.setHttpListenHost(controllerConfig.getHttpServerIp());

        HttpServer httpServer = httpServerFactory.getHttpServer(config);
        httpServer.start();

        logger.info("Controller started listening on port " + httpServer.getHttpListenPort());

        jobFarmer.setListenAddress(httpServer.getHttpListenHost());
        jobFarmer.setListenPort(httpServer.getHttpListenPort());
    }
}
