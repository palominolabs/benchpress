package com.palominolabs.benchpress.example.multidb.hbase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.example.multidb.key.KeyGen;
import com.palominolabs.benchpress.example.multidb.task.TaskConfigBase;
import com.palominolabs.benchpress.example.multidb.value.ValueGen;
import com.palominolabs.benchpress.job.task.TaskOperation;

import javax.annotation.concurrent.Immutable;

@Immutable
class HBaseConfig extends TaskConfigBase {
    private final String zkQuorum;
    private final int zkPort;
    private final String table;
    private final String columnFamily;
    private final String qualifier;
    private final boolean autoFlush;
    private final Long writeBufferSize;

    @JsonCreator
    HBaseConfig(@JsonProperty("op") TaskOperation taskOperation, @JsonProperty("threads") int numThreads,
        @JsonProperty("quanta") int numQuanta, @JsonProperty("batchSize") int batchSize,
        @JsonProperty("keyGen") KeyGen keyGen, @JsonProperty("valueGen") ValueGen valueGen,
        @JsonProperty("zkQuorum") String zkQuorum, @JsonProperty("zkPort") int zkPort,
        @JsonProperty("table") String table, @JsonProperty("columnFamily") String columnFamily,
        @JsonProperty("qualifier") String qualifier, @JsonProperty("autoFlush") boolean autoFlush,
        @JsonProperty("writeBufferSize") Long writeBufferSize) {
        super(taskOperation, numThreads, numQuanta, batchSize, keyGen, valueGen);
        this.zkQuorum = zkQuorum;
        this.zkPort = zkPort;
        this.table = table;
        this.columnFamily = columnFamily;
        this.qualifier = qualifier;
        this.autoFlush = autoFlush;
        this.writeBufferSize = writeBufferSize;
    }

    @JsonProperty("autoFlush")
    boolean isAutoFlush() {
        return autoFlush;
    }

    @JsonProperty("columnFamily")
    String getColumnFamily() {
        return columnFamily;
    }

    @JsonProperty("qualifier")
    String getQualifier() {
        return qualifier;
    }

    @JsonProperty("table")
    String getTable() {
        return table;
    }

    @JsonProperty("writeBufferSize")
    Long getWriteBufferSize() {
        return writeBufferSize;
    }

    @JsonProperty("zkPort")
    int getZkPort() {
        return zkPort;
    }

    @JsonProperty("zkQuorum")
    String getZkQuorum() {
        return zkQuorum;
    }

    @Override
    public HBaseConfig withNewQuanta(int newQuanta) {
        return new HBaseConfig(getTaskOperation(), getNumThreads(), newQuanta, getBatchSize(), getKeyGen(),
            getValueGen(), zkQuorum, zkPort, table, columnFamily, qualifier, autoFlush,
            writeBufferSize);
    }
}
