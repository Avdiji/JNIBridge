package com.jnibridge.examples.mappings.simple;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.mapping.MethodName;
import com.jnibridge.annotations.mapping.UseMapping;
import com.jnibridge.mapper.standard.string.StringViewMapper;

@BridgeClass(namespace = "jnibridge::examples")
public class SimpleStaticMappings {

    @MethodName("voidFunction")
    public static native void nativeVoidFunction();

    @MethodName("incrementInt")
    public static native int nativeIncrementInt(final int value);

    @MethodName("incrementShort")
    public static native short nativeIncrementShort(final short value);

    @MethodName("incrementFloat")
    public static native int nativeIncrementFloat(final float value);

    @MethodName("incrementLong")
    public static native int nativeIncrementLong(final long value);

    @MethodName("incrementDouble")
    public static native int nativeIncrementDouble(final double value);

    public static native boolean isTrue(final boolean value);

    public static native char getNextChar(final char value);

    public static native String getFunnyString(final String value);

    @UseMapping(StringViewMapper.class)
    public static native String getStringView(final String value);

    // Inner class to be mapped
    @BridgeClass(namespace = "jnibridge::examples")
    public static class InnerClass {

        public static native String getFunnyString(final String value);
    }
}
