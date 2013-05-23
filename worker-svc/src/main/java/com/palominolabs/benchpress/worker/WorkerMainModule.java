package com.palominolabs.benchpress.worker;

import com.google.inject.AbstractModule;
import com.palominolabs.benchpress.curator.InstanceSerializerModule;
import com.palominolabs.benchpress.http.server.DefaultJerseyServletModule;
import com.palominolabs.benchpress.ipc.IpcHttpClientModule;
import com.palominolabs.benchpress.ipc.IpcJsonModule;
import com.palominolabs.benchpress.job.key.DefaultKeyGeneratorFactoriesModule;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.value.DefaultValueGeneratorFactoryFactoriesModule;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.job.task.TaskFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.task.cassandra.CassandraModule;
import com.palominolabs.benchpress.task.hbase.HbaseModule;
import com.palominolabs.benchpress.task.hbaseAsync.HbaseAsyncModule;
import com.palominolabs.benchpress.task.mongodb.MongoDbModule;
import com.palominolabs.benchpress.task.reporting.TaskProgressClientModule;
import com.palominolabs.benchpress.worker.http.ResourceModule;
import com.palominolabs.benchpress.zookeeper.CuratorModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.http.server.HttpServerModule;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import org.apache.commons.configuration.SystemConfiguration;

public final class WorkerMainModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(WorkerMain.class);

        install(new HttpServerModule());

        bind(MetricsRegistry.class).toInstance(Metrics.defaultRegistry());

        install(new DefaultJerseyServletModule());

        install(new ResourceModule());
        install(new ConfigModuleBuilder().addConfiguration(new SystemConfiguration()).build());

        install(new CuratorModule());

        install(new InstanceSerializerModule());

        install(new IpcHttpClientModule());
        install(new IpcJsonModule());
        install(new TaskProgressClientModule());
        install(new JobRegistryModule());

        bind(PartitionRunner.class);

        ConfigModule.bindConfigBean(binder(), WorkerConfig.class);

        install(new KeyGeneratorFactoryFactoryRegistryModule());
        install(new ValueGeneratorFactoryFactoryRegistryModule());
        install(new DefaultKeyGeneratorFactoriesModule());
        install(new DefaultValueGeneratorFactoryFactoriesModule());

        install(new TaskFactoryFactoryRegistryModule());
        install(new HbaseAsyncModule());
        install(new HbaseModule());
        install(new CassandraModule());
        install(new MongoDbModule());
    }

}
