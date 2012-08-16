package com.palominolabs.benchpress.task.hbaseAsync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

import java.io.IOException;

@Id("HBASE_ASYNC")
final class HbaseAsyncTaskFactoryFactory implements TaskFactoryFactory {

    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        HBaseAsyncConfig c = objectReader.withType(HBaseAsyncConfig.class).readValue(configNode);
        return new HbaseAsyncTaskFactory(c);
    }

    static class HBaseAsyncConfig {
        @JsonProperty("zkQuorum")
        String zkQuorum;
        @JsonProperty("table")
        String table;
        @JsonProperty("columnFamily")
        String columnFamily;
        @JsonProperty("qualifier")
        String qualifier;
    }
}
