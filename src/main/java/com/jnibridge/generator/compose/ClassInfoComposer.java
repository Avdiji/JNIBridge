package com.jnibridge.generator.compose;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.generator.compose.jni.helper.JniBridgeExceptionComposer;
import com.jnibridge.generator.compose.jni.MethodInfoJNIComposer;

import com.jnibridge.generator.compose.jni.helper.polymorphism.PolymorphicHelperComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.utils.ResourceUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Composes string representations of {@link ClassInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class ClassInfoComposer implements Composer {

    public static final String PLACEHOLDER_INTERNAL_INCLUDES = "internal_includes";

    public static final String PLACEHOLDER_CUSTOM_JNI_CONTENT = "customJNIContent";
    public static final String PLACEHOLDER_METHODS = "mappedMethods";
    public static final String PLACEHOLDER_FULL_J_PATH = "fullJPath";

    @NonNull
    private final ClassInfo classInfo;

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        replacements.put(PLACEHOLDER_INTERNAL_INCLUDES, computeInternalInclude());
        replacements.put(PLACEHOLDER_CUSTOM_JNI_CONTENT, getCustomJNIContent());

        replacements.put(PLACEHOLDER_METHODS, getMappedMethods());
        replacements.put(PLACEHOLDER_FULL_J_PATH, classInfo.getClazz().getName().replace(".", "/"));

        return replacements;
    }

    /**
     * Compose the replacement for all class specific custom JNI-code.
     *
     * @return A replacement for the CustomJNICode placeholder.
     */
    private String getCustomJNIContent() {
        Class<?> classToBridge = classInfo.getClazz();
        BridgeClass annotation = classToBridge.getAnnotation(BridgeClass.class);
        Objects.requireNonNull(annotation, "Missing 'BridgeClass' annotation!");

        StringBuilder result = new StringBuilder();
        for (final String resourcePath : annotation.customJniCodePaths()) {
            result.append(ResourceUtils.load(resourcePath));
        }

        return result.toString();
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

        StringBuilder relativeParentPath = new StringBuilder();
        for (int i = 0; i < slashes + 1; ++i) {
            relativeParentPath.append("../");
        }

        String internalIncludeTemplate = "#include " + "\"" + relativeParentPath + "internal/%s\"";
        result.append(String.format(internalIncludeTemplate, PolymorphicHelperComposer.POLYMORPHIC_CONVENIENCE_HEADER_FILENAME))
                .append("\n")
                .append(String.format(internalIncludeTemplate, JniBridgeExceptionComposer.INTERNAL_FILENAME));

        return result.toString();
    }
}
