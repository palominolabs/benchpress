package com.palominolabs.benchpress.job.id;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Set;

/**
 * Subclass this class and request Set<T> ctor injection of an {@link Identifiable} class.
 *
 * @param <T>
 */
@ThreadSafe
public abstract class IdRegistry<T extends Identifiable> {

    private final Map<String, T> map = Maps.newHashMap();

    protected IdRegistry(Set<T> instances) {

        for (T t : instances) {
            String id = t.getRegistryId();

            if (map.containsKey(id)) {
                throw new IllegalArgumentException(
                        t.getClass().getName() + " has id <" + id + "> which conflicts with " +
                                map.get(id).getClass().getName());
            }

            map.put(id, t);
        }
    }

    /**
     * @param id a string id that some T impl class has conceivably been annotated with (and bound in Guice)
     * @return a T that was annotated with @Id(id)
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
