package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Ptr;
import com.jnibridge.annotations.lifecycle.Ref;

@BridgeClass(namespace = "jnibridge::examples")
public class B extends A {
    public B() { allocB(); }

    @Allocate
    private native void allocB();
}
