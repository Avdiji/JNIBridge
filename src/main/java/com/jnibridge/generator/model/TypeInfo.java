package com.jnibridge.generator.model;

import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.List;

@Getter
@Builder
public class TypeInfo {

    private final Class<?> type;

    private final List<Annotation> annotations;

    private String cType;
    private String inMapping;
    private String outMapping;
}
