package com.palominolabs.benchpress;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Path("simple-http-test-endpoint")
@Singleton
public final class SimpleHttpResource {

    AtomicInteger counter = new AtomicInteger();

    @Inject
    SimpleHttpResource() {
    }

    @GET
    public String get() {
        counter.incrementAndGet();
        return "ok";
    }
}
