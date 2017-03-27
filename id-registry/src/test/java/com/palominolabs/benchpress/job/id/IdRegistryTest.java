package com.palominolabs.benchpress.job.id;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.multibindings.Multibinder;
import java.util.Set;
import javax.annotation.Nonnull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public final class IdRegistryTest {

    @Test
    public void testNoImplsBoundDoesntCrash() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(FooRegistry.class);
                Multibinder.newSetBinder(binder(), Foo.class);
            }
        });
        FooRegistry registry = injector.getInstance(FooRegistry.class);

        try {
            registry.get("foo");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown id <foo>", e.getMessage());
        }
    }

    @Test
    public void testDetectsConflict() {
        try {
            Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
                @Override
                protected void configure() {
                    binder().requireExplicitBindings();
                    bind(FooRegistry.class);

                    Multibinder.newSetBinder(binder(), Foo.class).addBinding().to(Conflict1.class);
                    Multibinder.newSetBinder(binder(), Foo.class).addBinding().to(Conflict2.class);
                }
            }).getInstance(FooRegistry.class);

            fail();
        } catch (ProvisionException e) {
            assertEquals("Error injecting constructor, java.lang.IllegalArgumentException: " +
                            Conflict2.class.getName() + " has id <conflict> which conflicts with " + Conflict1.class.getName(),
                    e.getErrorMessages().iterator().next().getMessage());
        }
    }

    @Test
    public void testWorks() {
        Foo shouldWork =
                Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
                    @Override
                    protected void configure() {
                        binder().requireExplicitBindings();
                        bind(FooRegistry.class);
                        Multibinder.newSetBinder(binder(), Foo.class).addBinding().to(ShouldWork.class);
                    }
                }).getInstance(FooRegistry.class).get("shouldWork");

        assertNotNull(shouldWork);
        assertEquals((ShouldWork.class), shouldWork.getClass());
    }

    private static class FooRegistry extends IdRegistry<Foo> {

        @Inject
        protected FooRegistry(Set<Foo> instances) {
            super(instances);
        }
    }

    private interface Foo extends Identifiable {

    }

    private static class Conflict1 implements Foo {

        @Nonnull
        @Override
        public String getRegistryId() {
            return "conflict";
        }
    }

    private static class Conflict2 implements Foo {

        @Nonnull
        @Override
        public String getRegistryId() {
            return "conflict";
        }
    }

    private static class ShouldWork implements Foo {

        @Nonnull
        @Override
        public String getRegistryId() {
            return "shouldWork";
        }
    }
}
