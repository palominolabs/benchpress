package com.palominolabs.benchpress.task.cassandra;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

import java.io.IOException;

@Id("CASSANDRA")
final class CassandraTaskFactoryFactory implements TaskFactoryFactory {

    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        CassandraConfig c = objectReader.withType(CassandraConfig.class).readValue(configNode);
        return new CassandraTaskFactory(c);
    }

    static class CassandraConfig {
        @JsonProperty("cluster")
        String cluster;
        @JsonProperty("keyspace")
        String keyspace;
        @JsonProperty("port")
        int port;
        @JsonProperty("seeds")
        String seeds;
        @JsonProperty("columnFamily")
        String columnFamily;
        @JsonProperty("column")
        String column;
    }
}
