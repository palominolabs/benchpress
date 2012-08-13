package com.palominolabs.http.server;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.GuiceFilter;

public class HttpServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GuiceFilter.class);
        bind(HttpServerFactory.class);
    }
}
