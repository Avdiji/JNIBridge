package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;
import com.jnibridge.annotations.lifecycle.Ref;
import com.jnibridge.annotations.lifecycle.Shared;
import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.nativeaccess.Pointer;

@BridgeClass(namespace = "jnibridge::examples", customJniCodePaths = "someJNICode.mapping")
public class BaseClass extends Pointer {

    public BaseClass() {  allocBase(); }

    @Allocate
    private native void allocBase();

    @Override
    @Deallocate
    public native void destruct();

    public native String getString();
    public native void throwNestedError();

    @Ref
    @Const
    public native BaseClass getThisRef();

    public static native void printString(@Shared BaseClass other);
}
