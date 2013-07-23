package com.palominolabs.benchpress.job.json;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

@Immutable
public final class KeyGen {
    private final Map<String, Object> config;

    @JsonProperty("type")
    public final String keyGenType;

    @JsonCreator
    public KeyGen(@JsonProperty("config") Map<String, Object> config, @JsonProperty("type") String keyGenType) {
        this.config = config == null ? null : new ImmutableMap.Builder<String, Object>().putAll(config).build();
        this.keyGenType = keyGenType;
    }

    @JsonProperty("config")
    public Map<String, Object> getJsonConfig() {
        return config;
    }

    /**
     * @return dev-friendly version of config
     */
    public Configuration getConfig() {
        return new MapConfiguration(config);
    }
}
