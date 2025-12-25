package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.*;

@BridgeClass(namespace = "jnibridge::examples")
public class B extends A {
    public B() { allocB(); }

    @Allocate
    @Unique
    private native void allocB();
}
