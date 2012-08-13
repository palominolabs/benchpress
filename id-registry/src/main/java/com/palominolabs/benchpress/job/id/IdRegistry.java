package com.palominolabs.benchpress.job.id;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Set;

/**
 * Subclass this class and request Set<T> ctor injection of a class that should be annotated with {@link Id}.
 *
 * @param <T>
 */
@ThreadSafe
public abstract class IdRegistry<T> {

    private final Map<String, T> map = Maps.newHashMap();

    protected IdRegistry(Set<T> instances) {

        for (T t : instances) {
            Id annotation = t.getClass().getAnnotation(Id.class);
            if (annotation == null) {
                throw new IllegalArgumentException(
                    t.getClass().getName() + " is not annotated with " + Id.class.getName());
            }

            if (map.containsKey(annotation.value())) {
                throw new IllegalArgumentException(
                    t.getClass().getName() + " is annotated with " + annotation + " which conflicts with " +
                        map.get(annotation.value()).getClass().getName());
            }

            map.put(annotation.value(), t);
        }
    }

    /**
     * @param id a string id that some T impl class has conceivably been annotated with (and bound in Guice)
     * @return a T that was annotated with @Id(id)
     * @throws IllegalArgumentException
     */
    @Nonnull
    public T get(String id) throws IllegalArgumentException {
        T t = map.get(id);
        if (t == null) {
            throw new IllegalArgumentException("Unknown id <" + id + ">");
        }
        return t;
    }
}
