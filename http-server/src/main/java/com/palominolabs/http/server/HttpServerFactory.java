package com.palominolabs.http.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.GuiceFilter;

public class HttpServerFactory {

    private final Provider<GuiceFilter> guiceFilterProvider;

    @Inject
    HttpServerFactory(Provider<GuiceFilter> guiceFilterProvider) {
        this.guiceFilterProvider = guiceFilterProvider;
    }

    public HttpServer getHttpServer(HttpServerConfig config) {
        return new HttpServer(config, guiceFilterProvider);
    }

}
