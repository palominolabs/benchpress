package com.palominolabs.benchpress.jersey;

import com.google.inject.Injector;
import com.squarespace.jersey2.guice.JerseyGuiceModule;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;

/**
 * Jersey's SPI is a pain. See https://github.com/Squarespace/jersey2-guice/wiki
 *
 * This will override any auto-generated HK2 locators with one backed by the provided injector (via a child injector to
 * incorporate the auto-generated name which isn't known beforehand). This is useful when multiple HK2 initializations
 * are performed per JVM (e.g. in integration tests).
 *
 * This isn't strictly needed in this demo but it's good to know how to do it if you take this project and add
 * integration tests that use the http stack, etc.
 */
public class GuiceServiceLocatorGenerator implements ServiceLocatorGenerator {

    private final Injector injector;

    public GuiceServiceLocatorGenerator(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ServiceLocator create(String name, ServiceLocator parent) {
        if (!name.startsWith("__HK2_Generated_")) {
            return null;
        }

        Injector childInjector = injector.createChildInjector(new JerseyGuiceModule(name));

        return childInjector.getInstance(ServiceLocator.class);
    }
}
