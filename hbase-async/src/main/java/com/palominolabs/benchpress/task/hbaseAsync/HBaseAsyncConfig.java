package com.palominolabs.benchpress.task.hbaseAsync;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palominolabs.benchpress.job.base.task.TaskConfigBase;
import com.palominolabs.benchpress.job.json.KeyGen;
import com.palominolabs.benchpress.job.json.ValueGen;
import com.palominolabs.benchpress.job.task.TaskOperation;

import javax.annotation.concurrent.Immutable;

@Immutable
class HBaseAsyncConfig extends TaskConfigBase {
    private final String zkQuorum;
    private final String table;
    private final String columnFamily;
    private final String qualifier;

    @JsonCreator
    HBaseAsyncConfig(@JsonProperty("op") TaskOperation taskOperation, @JsonProperty("threads") int numThreads,
        @JsonProperty("quanta") int numQuanta, @JsonProperty("batchSize") int batchSize,
        @JsonProperty("keyGen") KeyGen keyGen, @JsonProperty("valueGen") ValueGen valueGen,
        @JsonProperty("zkQuorum") String zkQuorum,
        @JsonProperty("table") String table, @JsonProperty("columnFamily") String columnFamily,
        @JsonProperty("qualifier") String qualifier) {
        super(taskOperation, numThreads, numQuanta, batchSize, keyGen, valueGen);
        this.zkQuorum = zkQuorum;
        this.table = table;
        this.columnFamily = columnFamily;
        this.qualifier = qualifier;
    }

    @JsonProperty("columnFamily")
    public String getColumnFamily() {
        return columnFamily;
    }

    @JsonProperty("qualifier")
    public String getQualifier() {
        return qualifier;
    }

    @JsonProperty("table")
    public String getTable() {
        return table;
    }

    @JsonProperty("zkQuorum")
    public String getZkQuorum() {
        return zkQuorum;
    }

    @Override
    public HBaseAsyncConfig withNewQuanta(int newQuanta) {
        return new HBaseAsyncConfig(getTaskOperation(), getNumThreads(), newQuanta, getBatchSize(), getKeyGen(),
            getValueGen(), zkQuorum, table, columnFamily, qualifier);
    }
}
