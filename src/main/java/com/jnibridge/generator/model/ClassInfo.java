package com.jnibridge.generator.model;

import com.jnibridge.annotations.BridgeClass;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a model of a Java class annotated with {@link BridgeClass}, used in JNI-compatible
 * code generation.
 */
@Getter
@Builder
public class ClassInfo implements Comparable<ClassInfo> {

    // @formatter:off
    @NonNull  private final Class<?> clazz;
    @NonNull private final List<MethodInfo> methodsToMap; // <- excluding inherited methods
    @NonNull private final SortedSet<Class<?>> subclasses; // <- including itself
    @NonNull private final String fullCType;
    // @formatter:on

    @Override
    public int compareTo(@NotNull ClassInfo other) {
        // Sort in a way where superclasses are in the end of the list...
        // used to properly implement the polymorphic helpers...
        if (this.clazz.equals(other.clazz)) { return 0; }
        if(this.clazz.isAssignableFrom(other.clazz)) { return 1; }
        if(other.clazz.isAssignableFrom(this.clazz)) { return -1; }

        return this.clazz.getName().compareTo(other.clazz.getName());
    }
}
