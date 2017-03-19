package com.palominolabs.benchpress.ipc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.AbstractModule;

public final class IpcJsonModule extends AbstractModule {
    @Override
    protected void configure() {
        ObjectMapper objectMapper = new ObjectMapper();

        bind(ObjectReader.class).annotatedWith(Ipc.class).toInstance(objectMapper.reader());
        bind(ObjectWriter.class).annotatedWith(Ipc.class).toInstance(objectMapper.writer());

        // use ipc object mapper in Jersey
        final JacksonJsonProvider provider = new JacksonJsonProvider();
        provider.setMapper(objectMapper);
        bind(JacksonJsonProvider.class).toInstance(provider);
    }
}
