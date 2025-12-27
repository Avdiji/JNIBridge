package com.jnibridge.generator.model;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * Represents a model of a Java type, used in JNI-compatible
 * code generation.
 */
@Getter
@Builder
public class TypeInfo {

    // @formatter:off
    @NonNull private final Class<?> type;


    @Nullable private final String id;
    private final boolean isInvoker; // <- is this type invoking the native function?

    @NonNull private final String cType;
    @NonNull private final String jniType;

    @Setter
    @Nullable private String cleanupLogic; // <- to properly release jni elements...

    @Setter
    @Nullable private String inMapping;

    @Setter
    @Nullable private String outMapping;

    @NonNull private final List<Annotation> annotations;
    // @formatter:on

    /**
     * @param annotationClass The annotation-class to find.
     * @return True if this TypeInfo is annotated with the passed annotationClass
     */
    public boolean hasAnnotation(@NotNull final Class<?> annotationClass) {
        return annotations.stream().anyMatch(a -> a.annotationType().equals(annotationClass));
    }

    /**
     * Retrieves an annotation of the specified type from this element, if present.
     *
     * <p>This method searches the internal annotation collection and returns the
     * first annotation whose type exactly matches the provided {@code annotationClass}.
     *
     * @param <T> the type of the annotation
     * @param annotationClass the {@link Class} object corresponding to the annotation type
     *                        to retrieve; must not be {@code null}
     * @return an {@link Optional} containing the matching annotation if present,
     *         or {@link Optional#empty()} if no such annotation exists
     */
    @NotNull
    public <T extends Annotation> Optional<T> getAnnotation(@NotNull final Class<T> annotationClass) {
        return annotations.stream()
                .filter(a -> a.annotationType() == annotationClass)
                .map(annotationClass::cast)
                .findFirst();
    }
}
