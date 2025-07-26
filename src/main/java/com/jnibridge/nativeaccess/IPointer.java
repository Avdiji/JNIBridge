package com.jnibridge.nativeaccess;

/**
 * Interface enables native access to objects and variables, and manages object lifetime.
 */
public interface IPointer {

    /**
     * @return The pointer of the underlying native object.
     */
    long getNativeHandle();

    /**
     * Set the native handle of this object.
     *
     * @param handle The new handle.
     */
    void setNativeHandle(final long handle);

    /**
     * Calls the native destructor of the mapped object.
     */
    void destruct();
}
