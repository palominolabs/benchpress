package com.palominolabs.benchpress.guice;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Module that instantiates String module class names and installs them
 */
public final class ReflectiveModuleInstantiationModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(ReflectiveModuleInstantiationModule.class);

    private final List<String> moduleClasses;

    /**
     * @param moduleClasses class names to install
     */
    public ReflectiveModuleInstantiationModule(List<String> moduleClasses) {
        this.moduleClasses = ImmutableList.copyOf(moduleClasses);
    }

    /**
     * @param moduleNames comma-separated string of fully-qualified module names, or null
     * @return a Module that installs the specified modules, if any, or a no-op module
     */
    @Nonnull
    public static Module getModuleForModuleNamesString(@Nullable String moduleNames) {
        if (moduleNames != null) {
            return new ReflectiveModuleInstantiationModule(
                Lists.newArrayList(moduleNames.split(",")));
        }

        // otherwise, no op module
        return new AbstractModule() {
            @Override
            protected void configure() {
            }
        };
    }

    @Override
    protected void configure() {
        for (String moduleClassName : moduleClasses) {
            logger.debug("Creating module class " + moduleClassName);
            Class<?> origClass;
            try {
                origClass = Class.forName(moduleClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }

            Class<? extends Module> moduleClass;
            try {
                moduleClass = origClass.asSubclass(Module.class);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(origClass.getName() + " is not a " + Module.class.getName());
            }

            Constructor<? extends Module> constructor;
            try {
                constructor = moduleClass.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No 0-arg ctor found for " + moduleClass.getName(), e);
            }

            Module module;
            try {
                module = constructor.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Are you trying to instantiate an abstract class? Don't do that.",
                    e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("The 0-arg ctor must be public", e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Module instantiation failed", e);
            }

            install(module);
        }
    }
}
