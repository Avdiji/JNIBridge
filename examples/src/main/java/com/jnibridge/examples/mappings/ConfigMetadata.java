package com.jnibridge.examples.mappings;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.BridgeMetadata;

@BridgeClass(
        metadata = @BridgeMetadata(
                includes = "../../../../../../../native/simple/SimpleStatics.cpp",
                customJNICodePaths = "someJNICode.mapping"
        )
)
public class ConfigMetadata {
}
