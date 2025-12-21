package com.jnibridge.examples.mappings.diamond;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.examples.mappings.oop.BaseClass;
import com.jnibridge.nativeaccess.IPointer;

@BridgeClass
public interface Animal extends IPointer {
    void eat();
}
