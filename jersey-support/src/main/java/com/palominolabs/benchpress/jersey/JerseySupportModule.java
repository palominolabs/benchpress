package com.palominolabs.benchpress.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.http.server.HttpServerWrapperModule;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Sets up the default Jersey / Guice / Jackson integration.
 *
 * Users should bind {@link ServletContainer} to an instance constructed with the Jersey app (probably a subclass of
 * {@link JerseyResourceConfigBase} to serve.
 */
public class JerseySupportModule extends ServletModule {
    @Override
    protected void configureServlets() {
        install(new HttpServerWrapperModule());

        serve("/*").with(ServletContainer.class);
    }

    /**
     * Provide a ContextResolver to be used in a ResourceConfig (like {@link JerseyResourceConfigBase} that uses {@link
     * Ipc} object mapper in Jersey
     */
    @Ipc
    @Provides
    @Singleton
    ObjectMapperContextResolver getOMCR(@Ipc ObjectMapper objectMapper) {
        return new ObjectMapperContextResolver(objectMapper);
    }
}
