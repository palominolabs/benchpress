package com.palominolabs.benchpress.controller.zookeeper;

import com.google.inject.AbstractModule;
import com.palominolabs.config.ConfigModule;

public final class ZKServerModule extends AbstractModule {
    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), ZKServerConfig.class);
        bind(ZKServer.class);
    }
}
