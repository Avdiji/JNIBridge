package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.BridgeMetadata;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;
import com.jnibridge.nativeaccess.Pointer;

@BridgeClass(
        metadata = @BridgeMetadata(
                includes = "../../../../../../../native/oop/AnotherClass.cpp"
        ),
        namespace = "jnibridge::examples"
)
public class AnotherClass extends Pointer {

    public AnotherClass() { allocBase(); }

    @Allocate
    private native void allocBase();

    @Override
    @Deallocate
    public native void destruct();
}
