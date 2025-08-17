package com.jnibridge.examples.mappings.simple;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.Name;
import com.jnibridge.annotations.typemapping.UseMapping;
import com.jnibridge.mapper.standard.StringViewMapper;

@BridgeClass(namespace = "jnibridge::examples")
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
