package com.jnibridge.generator.compose;

import com.jnibridge.generator.compose.jni.MethodInfoJNIComposer;
import com.jnibridge.generator.compose.polymorphism.PolymorphicHelperComposer;
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

    public static final String PLACEHOLDER_INTERNAL_INCLUDE = "internal_include";

    public static final String PLACEHOLDER_METHODS = "mappedMethods";
    public static final String PLACEHOLDER_FULL_J_PATH = "fullJPath";

    @NonNull
    private final ClassInfo classInfo;

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        replacements.put(PLACEHOLDER_INTERNAL_INCLUDE, computeInternalInclude());
        replacements.put(PLACEHOLDER_METHODS, getMappedMethods());
        replacements.put(PLACEHOLDER_FULL_J_PATH, classInfo.getClazz().getName().replace(".", "/"));

        return replacements;
    }

    /**
     * @return A String concatenation of all the methods to be mapped as JNI-code.
     */
    private String getMappedMethods() {
        return classInfo.getMethodsToMap()
                .stream()
                .map(methodInfo -> new MethodInfoJNIComposer(methodInfo).compose())
                .collect(Collectors.joining("\n"));
    }

    /**
     * @return the relative include path to the JNIBridge-Helper file.
     */
    private String computeInternalInclude() {
        String packagePath = classInfo.getClazz().getPackage().getName();
        long slashes = packagePath.chars().filter(ch -> ch == '.').count();

        StringBuilder result = new StringBuilder();

        for(int i = 0; i < slashes + 1; ++i) {
            result.append("../");
        }

        result.append(String.format("internal/%s",PolymorphicHelperComposer.POLYMORPHIC_CONVENIENCE_HEADER_FILENAME));
        return result.toString();
    }
}
