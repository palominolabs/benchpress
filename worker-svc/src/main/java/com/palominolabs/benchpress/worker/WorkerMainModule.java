package com.palominolabs.benchpress.worker;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.ipc.IpcHttpClientModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.task.TaskPluginRegistryModule;
import com.palominolabs.benchpress.task.reporting.TaskProgressClientModule;
import com.palominolabs.benchpress.worker.http.WorkerResourceModule;
import com.palominolabs.benchpress.curator.CuratorModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.config.ConfigModuleBuilder;
import org.apache.commons.configuration.SystemConfiguration;

public final class WorkerMainModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(WorkerMain.class);

        install(new WorkerResourceModule());
        install(new ConfigModuleBuilder().addConfiguration(new SystemConfiguration()).build());

        install(new CuratorModule());

        install(new IpcHttpClientModule());
        install(new IpcJsonModule());
        install(new TaskProgressClientModule());
        install(new JobRegistryModule());
        install(new QueueProviderModule());

        bind(PartitionRunner.class);

        ConfigModule.bindConfigBean(binder(), WorkerConfig.class);

        install(new TaskPluginRegistryModule());
    }

}
