package com.jnibridge.nativeaccess;

import java.io.Closeable;

/**
 * Abstract base class for native-bound objects that simplifies {@link IPointer} implementation.
 * <p>
 * Provides default logic for storing and accessing a native handle, as well as optional finalization
 * for automatic native destruction.
 * <p>
 * This class is not required, but can be extended by any class that maps to a native object and wants
 * to avoid reimplementing boilerplate {@code IPointer} logic.
 *
 * <p><strong>Note:</strong> This class overrides {@link #finalize()} to call {@link #destructNativeHandle()}.
 * While convenient, finalization is discouraged in modern Java (deprecated in Java 9, removed in Java 18+)
 * and should be replaced with explicit lifecycle management or {@link java.lang.ref.Cleaner} where possible.
 * <p>
 * <strong>It is encouraged to implement your own {@code Pointer} subclass or utility that integrates
 * {@code Cleaner} for reliable and modern resource cleanup.</strong>
 */
public abstract class Pointer implements IPointer, Closeable {

    private long nativeHandle;

    @Override
    public long getNativeHandle() { return nativeHandle; }

    @Override
    public void setNativeHandle(long nativeHandle) { this.nativeHandle = nativeHandle; }

    @Override
    @SuppressWarnings("removal")
    protected void finalize() { destructNativeHandle(); }

    @Override
    public void close() { destructNativeHandle(); }
}
