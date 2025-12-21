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

    // the class to be mapped
    @NonNull  private final Class<?> clazz;

    // metadata
    @NonNull private final String nativeNamespace;
    @NonNull private final String nativeName;
    @NonNull private final String jName;

    // all the native methods (excluding the inherited ones...).
    @NonNull private final List<MethodInfo> methodsToMap;

    // all the subclasses
    @NonNull private final SortedSet<ClassInfo> subclasses;
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
