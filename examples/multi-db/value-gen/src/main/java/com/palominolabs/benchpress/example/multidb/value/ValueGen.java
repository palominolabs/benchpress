package com.palominolabs.benchpress.example.multidb.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

@Immutable
public final class ValueGen {
    private final String valueGenType;

    private final Map<String, Object> config;

    @JsonCreator
    public ValueGen(@JsonProperty("config") Map<String, Object> config, @JsonProperty("type") String valueGenType) {
        this.config = config == null ? null : new ImmutableMap.Builder<String, Object>().putAll(config).build();
        this.valueGenType = valueGenType;
    }

    @JsonProperty("config")
    public Map<String, Object> getJsonConfig() {
        return config;
    }

    @JsonProperty("type")
    public String getValueGenType() {
        return valueGenType;
    }

    /**
     * @return dev-friendly version of config
     */
    public Configuration getConfig() {
        return new MapConfiguration(config);
    }
}
