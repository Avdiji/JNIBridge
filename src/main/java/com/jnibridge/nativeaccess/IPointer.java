package com.jnibridge.nativeaccess;

/**
 * Represents a native-backed object that exposes access to its underlying memory address (native handle)
 * and provides explicit lifetime management.
 * <p>
 * Implementations of this interface are expected to map to native (e.g., C/C++) objects and enable direct
 * JNI access to fields or methods that operate on those native instances.
 * <p>
 * The {@code IPointer} interface is used by the JNI bridge to track object identity and ensure correct
 * construction, destruction, and pointer management on both the Java and native sides.
 */
public interface IPointer {

    /**
     * Returns the native memory address (handle) of the underlying native object.
     * <p>
     * This pointer is passed to JNI bindings to access native methods or fields.
     *
     * @return the raw native pointer value (as a {@code long})
     */
    long getNativeHandle();

    /**
     * Invokes the native destructor for the mapped object.
     * <p>
     * This should release any native memory or resources held by the object.
     * After calling this method, the native handle is invalidated.
     */
    void destruct();
}
