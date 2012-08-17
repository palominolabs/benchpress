package com.palominolabs.benchpress.guice;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;
import org.junit.Test;

import java.util.Collection;

import static com.palominolabs.benchpress.guice.ReflectiveModuleInstantiationModule.getModuleForModuleNamesString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class ReflectiveModuleInstantiationModuleTest {
    @Test
    public void testNonPublicCtor() {
        runExceptionTest(PrivateCtor.class, "No 0-arg ctor found for " + PrivateCtor.class.getName());
    }

    @Test
    public void testNotAModule() {
        runExceptionTest(NotAModule.class, NotAModule.class.getName() + " is not a " + Module.class.getName());
    }

    @Test
    public void testPublicCtor() {
        createInjector(PublicCtor.class).getInstance(BindTarget.class);
    }

    @Test
    public void testStaticStringHelperMethodTwoModules() {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(getModuleForModuleNamesString(PublicCtor.class.getName() + "," + PublicCtor2.class.getName()));
            }
        });

        injector.getInstance(BindTarget.class);
        injector.getInstance(Key.get(BindTarget.class, Names.named("foo")));
    }

    @Test
    public void testStaticStringHelperMethodOneModule() {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(getModuleForModuleNamesString(PublicCtor.class.getName()));
            }
        });

        injector.getInstance(BindTarget.class);
    }

    @Test
    public void testStaticStringHelperMethodNull() {
        Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(getModuleForModuleNamesString(null));
            }
        });

        // doesn't error
    }

    private static void runExceptionTest(Class<?> privateCtorClass, String message) {
        try {
            createInjector(privateCtorClass);
            fail();
        } catch (CreationException e) {
            Collection<Message> errorMessages = e.getErrorMessages();
            assertEquals(1, errorMessages.size());
            Throwable cause = errorMessages.iterator().next().getCause();
            assertEquals(
                message, cause.getMessage());
        }
    }

    private static Injector createInjector(final Class<?> privateCtorClass) {
        return Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(new ReflectiveModuleInstantiationModule(Lists.newArrayList(privateCtorClass.getName())));
            }
        });
    }

    private static class PublicCtor extends AbstractModule {

        public PublicCtor() {
        }

        @Override
        protected void configure() {
            bind(BindTarget.class);
        }
    }

    private static class PublicCtor2 extends AbstractModule {

        public PublicCtor2() {
        }

        @Override
        protected void configure() {
            bind(BindTarget.class).annotatedWith(Names.named("foo")).to(BindTarget.class);
        }
    }

    private static class NotAModule {

    }

    private static class BindTarget {
        BindTarget() {
        }
    }

    private static class PrivateCtor extends AbstractModule {

        private PrivateCtor() {
        }

        @Override
        protected void configure() {
            // no op
        }
    }
}
