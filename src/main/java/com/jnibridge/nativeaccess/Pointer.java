package com.jnibridge.nativeaccess;

/**
 * Interface enables native access to objects and variables, and manages object lifetime.
 */
public interface Pointer extends AutoCloseable {

    /**
     * @return The pointer of the underlying native object.
     */
    long getNativeHandle();

}
