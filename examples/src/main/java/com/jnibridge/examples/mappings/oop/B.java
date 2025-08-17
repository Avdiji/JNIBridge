package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;

@BridgeClass(namespace = "jnibridge::examples")
public class B extends A {
    public B() { allocB(); }

    @Allocate
    private native void allocB();

    public native B getThisInstance();

}
