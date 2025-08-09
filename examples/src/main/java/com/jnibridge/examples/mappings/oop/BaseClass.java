package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.BridgeMetadata;
import com.jnibridge.nativeaccess.Pointer;

@BridgeClass(
        metadata = @BridgeMetadata(
                includes = "../../../../../../../native/oop/BaseClass.cpp"
        ),
        namespace = "jnibridge::examples"
)
public class BaseClass extends Pointer {

        public native void printSomething();

        public native BaseClass asPtr(BaseClass other);

}
