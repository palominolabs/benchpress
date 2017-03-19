package com.palominolabs.benchpress.example.multidb.cassandra;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.job.json.KeyGen;
import com.palominolabs.benchpress.job.json.ValueGen;
import com.palominolabs.benchpress.job.task.TaskOperation;

import javax.annotation.concurrent.Immutable;

@Immutable
class CassandraConfig extends TaskConfigBase {
    private final String cluster;
    private final String keyspace;
    private final int port;
    private final String seeds;
    private final String columnFamily;
    private final String column;

    @JsonCreator
    CassandraConfig(@JsonProperty("op") TaskOperation taskOperation, @JsonProperty("threads") int numThreads,
        @JsonProperty("quanta") int numQuanta, @JsonProperty("batchSize") int batchSize,
        @JsonProperty("keyGen") KeyGen keyGen, @JsonProperty("valueGen") ValueGen valueGen,
        @JsonProperty("cluster") String cluster, @JsonProperty("keyspace") String keyspace,
        @JsonProperty("port") int port, @JsonProperty("seeds") String seeds,
        @JsonProperty("columnFamily") String columnFamily, @JsonProperty("column") String column) {
        super(taskOperation, numThreads, numQuanta, batchSize, keyGen, valueGen);

        this.cluster = cluster;
        this.keyspace = keyspace;
        this.port = port;
        this.seeds = seeds;
        this.columnFamily = columnFamily;
        this.column = column;
    }

    @JsonProperty("cluster")
    String getCluster() {
        return cluster;
    }

    @JsonProperty("column")
    String getColumn() {
        return column;
    }

    @JsonProperty("columnFamily")
    String getColumnFamily() {
        return columnFamily;
    }

    @JsonProperty("keyspace")
    String getKeyspace() {
        return keyspace;
    }

    @JsonProperty("port")
    int getPort() {
        return port;
    }

    @JsonProperty("seeds")
    String getSeeds() {
        return seeds;
    }

    @Override
    public CassandraConfig withNewQuanta(int newQuanta) {
        return new CassandraConfig(getTaskOperation(), getNumThreads(), newQuanta, getBatchSize(), getKeyGen(),
            getValueGen(), cluster, keyspace, port, seeds, columnFamily, column);
    }
}
