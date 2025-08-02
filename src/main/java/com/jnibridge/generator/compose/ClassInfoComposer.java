package com.jnibridge.generator.compose;

import com.jnibridge.generator.compose.jni.MethodInfoJNIComposer;
import com.jnibridge.generator.model.ClassInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Composes string representations of {@link ClassInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class ClassInfoComposer implements Composer {

    public static final String PLACEHOLDER_METHODS = "mappedMethods";
    public static final String PLACEHOLDER_MANGLED_CLASSPATH = "mangledClasspath";

    @NonNull
    private final ClassInfo classInfo;

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        replacements.put(PLACEHOLDER_METHODS,
                classInfo.getMethodsToMap()
                        .stream()
                        .map(methodInfo -> new MethodInfoJNIComposer(methodInfo).compose())
                        .collect(Collectors.joining("\n")));

        final String mangledClassPath = classInfo.getClazz().getName().replace(".", "_");
        replacements.put(PLACEHOLDER_MANGLED_CLASSPATH, "Java_" + mangledClassPath);

        return replacements;
    }
}
