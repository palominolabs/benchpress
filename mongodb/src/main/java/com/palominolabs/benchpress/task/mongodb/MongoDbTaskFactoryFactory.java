package com.palominolabs.benchpress.task.mongodb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;

import java.io.IOException;

@Id("MONGODB")
final class MongoDbTaskFactoryFactory implements TaskFactoryFactory {

    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        MongoDbConfig c = objectReader.withType(MongoDbConfig.class).readValue(configNode);
        return new MongoDbTaskFactory(c);
    }

    static class MongoDbConfig {
        @JsonProperty("hostname")
        String hostname;
        @JsonProperty("port")
        int port;
        @JsonProperty("dbName")
        String dbName;
        @JsonProperty("collectionName")
        String collectionName;
    }
}
