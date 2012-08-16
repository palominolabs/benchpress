package com.palominolabs.benchpress.worker;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.palominolabs.benchpress.job.key.DefaultKeyGeneratorFactoriesModule;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactory;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.job.registry.JobRegistryModule;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.job.value.DefaultValueGeneratorFactoryFactoriesModule;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.task.TaskFactoryFactoryRegistry;
import com.palominolabs.benchpress.task.TaskFactoryFactoryRegistryModule;
import com.palominolabs.benchpress.task.cassandra.CassandraModule;
import com.palominolabs.benchpress.task.hbase.HbaseModule;
import com.palominolabs.benchpress.task.hbaseAsync.HbaseAsyncModule;
import com.palominolabs.benchpress.task.reporting.NoOpTaskProgressClient;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogManager;

/**
 * Just a quick hack for testing stuff out.
 */
final class HbaseMain {

    private final TaskFactoryFactoryRegistry registry;

    private final TaskProgressClient taskProgressClient;

    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    @Inject
    HbaseMain(TaskFactoryFactoryRegistry registry, TaskProgressClient taskProgressClient,
        KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.registry = registry;
        this.taskProgressClient = taskProgressClient;
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                bind(TaskProgressClient.class).to(NoOpTaskProgressClient.class);
                bind(HbaseMain.class);
            }
        }, new JobRegistryModule(), new HbaseModule(), new CassandraModule(), new HbaseAsyncModule(),
            new TaskFactoryFactoryRegistryModule(), new KeyGeneratorFactoryFactoryRegistryModule(),
            new DefaultKeyGeneratorFactoriesModule(), new ValueGeneratorFactoryFactoryRegistryModule(),
            new DefaultValueGeneratorFactoryFactoriesModule());

        injector.getInstance(HbaseMain.class).run();
    }

    private void run() throws IOException {
        String tbl = "test-tbl";
        String columnFamily = "cf1";
        String qualifier = "qual";

        Map<String, Object> cassandraConfig = Maps.newHashMap();
        cassandraConfig.put("cluster", "cowpunk standalone cluster");
        cassandraConfig.put("keyspace", "test_ks");
        cassandraConfig.put("port", 9160);
        cassandraConfig.put("seeds", "127.0.1");
        cassandraConfig.put("columnFamily", "cf1");
        cassandraConfig.put("column", "col1");

//        HbaseTaskFactory hbaseTaskFactory = new HbaseTaskFactory("127.0.0.1", 2181, tbl, columnFamily, qualifier, true,
//            null);
//
//        HbaseAsyncTaskFactory hbaseAsyncTaskFactory = new HbaseAsyncTaskFactory("127.0.0.1:2181", tbl, columnFamily,
//            qualifier);    }

//        TaskFactory taskFactory = registry.get("CASSANDRA").getTaskFactory(new MapConfiguration(cassandraConfig));
//
//        go(taskFactory);
    }

    private void go(TaskFactory taskFactory) throws IOException {
        KeyGeneratorFactoryFactory keyGenerator = keyGeneratorFactoryFactoryRegistry.get("WORKER_ID_THREAD_ID_COUNTER");
        HashMap<String, Object> valueConfig = Maps.newHashMap();
        valueConfig.put("size", 100);
        ValueGeneratorFactory valueGenerator = valueGeneratorFactoryFactoryRegistry.get("ZERO_BYTE_ARRAY").getFactory(
            new MapConfiguration(valueConfig));

        Collection<Runnable> runnables = taskFactory
            .getRunnables(keyGenerator.getKeyGeneratorFactory(), valueGenerator, TaskOperation.WRITE, 1, 100, 10,
                UUID.randomUUID(), 1,
                taskProgressClient,
                UUID.randomUUID(), 100, new AtomicInteger());

        for (Runnable runnable : runnables) {
            runnable.run();
        }

        taskFactory.shutdown();
    }
}
