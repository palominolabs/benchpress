package com.palominolabs.benchpress.curator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.AbstractModule;

public final class InstanceSerializerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(InstanceSerializerFactory.class);

        ObjectMapper objectMapper = new ObjectMapper();
        bind(ObjectReader.class).annotatedWith(Curator.class).toInstance(objectMapper.reader());
        bind(ObjectWriter.class).annotatedWith(Curator.class).toInstance(objectMapper.writer());

    }
}
