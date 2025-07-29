package com.jnibridge.generator.model;


import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Builder
public class MethodInfo {

    private final Method method;

    private final String methodName;
    private final TypeInfo returnType;
    private final List<TypeInfo> params;

}
