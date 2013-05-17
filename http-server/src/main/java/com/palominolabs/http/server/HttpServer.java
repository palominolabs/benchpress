package com.palominolabs.http.server;

import ch.qos.logback.access.jetty.RequestLogImpl;
import com.google.inject.Provider;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final HttpServerConfig httpServerConfig;
    private final Provider<GuiceFilter> filterProvider;
    private final Server server = new Server();

    HttpServer(HttpServerConfig httpServerConfig, Provider<GuiceFilter> filterProvider) {
        this.httpServerConfig = httpServerConfig;
        this.filterProvider = filterProvider;
    }

    public void start() throws Exception {
        // servlet handler will contain the InvalidRequestServlet and the GuiceFilter
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.setContextPath("/");

        servletHandler.addServlet(new ServletHolder(new InvalidRequestServlet()), "/*");

        // add guice servlet filter
        FilterHolder guiceFilter = new FilterHolder(filterProvider.get());
        servletHandler.addFilter(guiceFilter, "/*", EnumSet.allOf(DispatcherType.class));

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(servletHandler);

        // add logback-access request log
        RequestLogHandler logHandler = new RequestLogHandler();
        RequestLogImpl logbackRequestLog = new RequestLogImpl();
        if (httpServerConfig.getAccessLogConfigFileInFilesystem() != null) {
            logger.debug("Setting logback access config fs path to " +
                httpServerConfig.getAccessLogConfigFileInFilesystem());
            logbackRequestLog.setFileName(httpServerConfig.getAccessLogConfigFileInFilesystem());
        } else if (httpServerConfig.getAccessLogConfigFileInClasspath() != null) {
            logger.debug("Loading logback access config from classpath path " + httpServerConfig
                .getAccessLogConfigFileInClasspath());
            logbackRequestLog.setResource(httpServerConfig.getAccessLogConfigFileInClasspath());
        } else {
            logger.warn("No access logging configured!");
        }
        logHandler.setRequestLog(logbackRequestLog);
        handlerCollection.addHandler(logHandler);

        server.setHandler(handlerCollection);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(httpServerConfig.getHttpListenPort());
        connector.setHost(httpServerConfig.getHttpListenHost());
        server.addConnector(connector);

        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public int getHttpListenPort() {
        return httpServerConfig.getHttpListenPort();
    }

    public String getHttpListenHost() {
        return httpServerConfig.getHttpListenHost();
    }
}
