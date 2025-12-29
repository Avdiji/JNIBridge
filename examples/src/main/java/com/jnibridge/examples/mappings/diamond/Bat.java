package com.jnibridge.examples.mappings.diamond;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;

import java.io.Closeable;

@BridgeClass
public class Bat implements Mammal, Bird, Closeable {

    // --------------- DEFAULT POINTER SETUP ---------------
    private long nativeHandle;

    // @formatter:off
    public Bat() { allocate(); }
    @Allocate public native void allocate();

    @Override public long getNativeHandle() { return this.nativeHandle; }
    @Override public void setNativeHandle(long nativeHandle) { this.nativeHandle = nativeHandle; }
    @Override public void close() { destructNativeHandle(); }

    @Override
    @Deallocate
    public native void destructNativeHandle();

    @Override
    @SuppressWarnings("removal")
    protected void finalize() throws Throwable {
        destructNativeHandle();
        super.finalize();
    }
    // @formatter:on

    // --------------- DEFAULT POINTER SETUP ---------------

    @Override
    public native void eat();


}
