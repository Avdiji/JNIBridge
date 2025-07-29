package com.jnibridge.generator.model;


import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Builder
public class MethodInfo {

    // the method to be mapped
    private final Method method;

    // metadata
    private final String namespace;
    private final String nativeName;
    private final String jName;

    // types used
    private final TypeInfo returnType;
    private final List<TypeInfo> params;

}
