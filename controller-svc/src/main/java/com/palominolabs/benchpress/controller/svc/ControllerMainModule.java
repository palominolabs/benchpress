package com.palominolabs.benchpress.controller.svc;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.controller.ControllerCoreModule;
import com.palominolabs.benchpress.controller.zookeeper.ZKServerModule;
import com.palominolabs.benchpress.curator.InstanceSerializerModule;
import com.palominolabs.benchpress.http.server.DefaultJerseyServletModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerModule;
import org.apache.commons.configuration.SystemConfiguration;

public final class ControllerMainModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(ControllerMain.class);

        install(new HttpServerModule());

        install(new DefaultJerseyServletModule());

        install(new ControllerCoreModule());
        install(new ConfigModuleBuilder().addConfiguration(new SystemConfiguration()).build());

        install(new CuratorModule());

        install(new InstanceSerializerModule());

        install(new IpcJsonModule());

        ConfigModule.bindConfigBean(binder(), ControllerConfig.class);

        install(new ZKServerModule());

        install(new KeyGeneratorFactoryFactoryRegistryModule());
        install(new ValueGeneratorFactoryFactoryRegistryModule());
        install(new TaskPluginRegistryModule());
    }
}
