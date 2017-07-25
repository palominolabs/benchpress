package com.palominolabs.benchpress.example.multidb.value;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Each thread gets its own ValueGenerator to avoid contention.
 */
@NotThreadSafe
public interface ValueGenerator {

    /**
     * @return byte array -- do not modify; ownership is retained by the ValueGenerator
     */
    byte[] getValue();

}
