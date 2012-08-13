package com.palominolabs.benchpress.job.id;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.multibindings.Multibinder;
import org.junit.Test;

import java.util.Set;

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
    public void testDetectsNoAnnotation() {

        try {
            Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
                @Override
                protected void configure() {
                    binder().requireExplicitBindings();
                    bind(FooRegistry.class);

                    Multibinder.newSetBinder(binder(), Foo.class).addBinding().to(NoAnnotationImpl.class);
                }
            }).getInstance(FooRegistry.class);

            fail();
        } catch (ProvisionException e) {
            assertEquals("Error injecting constructor, java.lang.IllegalArgumentException: " +
                NoAnnotationImpl.class.getName() + " is not annotated with " + Id.class.getName(),
                e.getErrorMessages().iterator().next().getMessage());
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
                Conflict2.class.getName() + " is annotated with @" + Id.class.getName() +
                "(value=conflict) which conflicts with " + Conflict1.class.getName(),
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

    private static interface Foo {

    }

    private static class NoAnnotationImpl implements Foo {

    }

    @Id("conflict")
    private static class Conflict1 implements Foo {

    }

    @Id("conflict")
    private static class Conflict2 implements Foo {

    }

    @Id("shouldWork")
    private static class ShouldWork implements Foo {

    }
}
