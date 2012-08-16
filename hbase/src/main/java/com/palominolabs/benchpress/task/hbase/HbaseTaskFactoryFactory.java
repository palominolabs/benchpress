package com.palominolabs.benchpress.task.hbase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

import java.io.IOException;

@Id("HBASE")
final class HbaseTaskFactoryFactory implements TaskFactoryFactory {

    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        HBaseConfig c = objectReader.withType(HBaseConfig.class).readValue(configNode);
        return new HbaseTaskFactory(c);
    }

    static class HBaseConfig {
        @JsonProperty("zkQuorum")
        String zkQuorum;
        @JsonProperty("zkPort")
        int zkPort;
        @JsonProperty("table")
        String table;
        @JsonProperty("columnFamily")
        String columnFamily;
        @JsonProperty("qualifier")
        String qualifier;
        @JsonProperty("autoFlush")
        boolean autoFlush;
        @JsonProperty("writeBufferSize")
        Long writeBufferSize;
    }
}
