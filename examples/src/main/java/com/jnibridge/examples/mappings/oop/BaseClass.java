package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;
import com.jnibridge.nativeaccess.Pointer;

@BridgeClass(namespace = "jnibridge::examples")
public class BaseClass extends Pointer {

    public BaseClass() { allocBase(); }

    @Allocate
    private native void allocBase();

    @Override
    @Deallocate
    public native void destruct();

    public native String getString();
}
