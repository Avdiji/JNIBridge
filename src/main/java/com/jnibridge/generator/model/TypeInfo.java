package com.jnibridge.generator.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

@Getter
@Builder
public class TypeInfo {

    // @formatter:off
    @NonNull private final Class<?> type;

    @Getter(AccessLevel.NONE)
    @NonNull private final List<Annotation> annotations;

    @Nullable private final String id;

    @NonNull private String cType;
    @NonNull private String jniType;

    @NonNull private String inMapping;
    @NonNull private String outMapping;

    // @formatter:on

    /**
     * @param annotationClass The annotation-class to find.
     * @return True if this TypeInfo is annotated with the passed annotationClass
     */
    public boolean hasAnnotation(Class<?> annotationClass) {
        return annotations.stream().anyMatch(a -> a.annotationType() == annotationClass);
    }
}
