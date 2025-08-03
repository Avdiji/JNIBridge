package com.jnibridge.examples.mappings.simple;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.InheritableMetadata;
import com.jnibridge.annotations.Name;

@BridgeClass(
        namespace = "jnibridge::examples",
        metadata = @InheritableMetadata(
                includes = "../../native/simple/SimpleStatics.cpp"
        )
)
public class SimpleStaticMappings {

    @Name("voidFunction")
    public static native void nativeVoidFunction();

    @Name("incrementInt")
    public static native int nativeIncrementInt(final int value);

    @Name("incrementShort")
    public static native short nativeIncrementShort(final short value);

    @Name("incrementFloat")
    public static native int nativeIncrementFloat(final float value);

    @Name("incrementLong")
    public static native int nativeIncrementLong(final long value);

    @Name("incrementDouble")
    public static native int nativeIncrementDouble(final double value);

    public static native char getNextChar(final char value);

    public static native String getFunnyString(final String value);

}
