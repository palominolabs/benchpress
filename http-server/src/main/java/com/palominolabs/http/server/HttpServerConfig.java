package com.palominolabs.http.server;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A simple bean to hold config info.
 */
@NotThreadSafe
public final class HttpServerConfig {

    private int httpListenPort = 8080;
    private String httpListenHost = "127.0.0.1";

    private String accessLogConfigFileInClasspath = "/" + this.getClass().getPackage()
        .getName().replace('.', '/') + "/default-logback-access.xml";

    private String accessLogConfigFileInFilesystem = null;

    public String getAccessLogConfigFileInClasspath() {
        return accessLogConfigFileInClasspath;
    }

    public String getAccessLogConfigFileInFilesystem() {
        return accessLogConfigFileInFilesystem;
    }

    public String getHttpListenHost() {
        return httpListenHost;
    }

    public void setHttpListenHost(String httpListenHost) {
        this.httpListenHost = httpListenHost;
    }

    public int getHttpListenPort() {
        return httpListenPort;
    }

    public void setHttpListenPort(int httpListenPort) {
        this.httpListenPort = httpListenPort;
    }
}
