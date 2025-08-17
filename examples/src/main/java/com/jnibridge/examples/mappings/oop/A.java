package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;

@BridgeClass(namespace = "jnibridge::examples")
public class A extends BaseClass {

    public A() { allocA(); }

    @Allocate
    private native void allocA();

}
