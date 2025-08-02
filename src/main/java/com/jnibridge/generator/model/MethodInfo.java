package com.jnibridge.generator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Builder
public class MethodInfo {

    // @formatter:off

    // the method to be mapped
    @NonNull private final Method method;

    // metadata
    private final boolean isStatic;

    @NonNull private final String namespace;
    @NonNull private final String nativeName;
    private final String jName;

    // types used
    @NonNull private final TypeInfo returnType;
    @NonNull private final List<TypeInfo> params;

    // @formatter:off
}
