package com.palominolabs.benchpress.worker;

import com.google.inject.AbstractModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.config.ConfigModuleBuilder;
import org.apache.commons.configuration.MapConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class TestConfigModule extends AbstractModule {
    private final ConfigModule configModule;

    public TestConfigModule(int port) {
        Map<String, Object> zkConfig = new HashMap<String, Object>();
        zkConfig.put("benchpress.zookeeper.client.connection-string", "localhost:" + port);
        configModule = new ConfigModuleBuilder().addConfiguration(new MapConfiguration(zkConfig)).build();
    }

    @Override
    protected void configure() {
        install(configModule);
    }
}
