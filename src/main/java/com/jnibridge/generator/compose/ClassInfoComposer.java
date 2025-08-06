package com.jnibridge.generator.compose;

import com.jnibridge.generator.compose.jni.MethodInfoJNIComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.utils.JNIMangler;
import com.jnibridge.utils.ResourceUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Composes string representations of {@link ClassInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class ClassInfoComposer implements Composer {

    public static final String PLACEHOLDER_INCLUDES = "nativeIncludes";
    public static final String PLACEHOLDER_CUSTOM_JNI_CODE = "customJNICode";

    public static final String PLACEHOLDER_METHODS = "mappedMethods";
    public static final String PLACEHOLDER_MANGLED_CLASSPATH = "mangledClasspath";

    @NonNull
    private final ClassInfo classInfo;

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();


        replacements.put(PLACEHOLDER_CUSTOM_JNI_CODE, getCustomJNICode());
        replacements.put(PLACEHOLDER_INCLUDES, getNativeIncludes());


        replacements.put(PLACEHOLDER_METHODS,
                classInfo.getMethodsToMap()
                        .stream()
                        .map(methodInfo -> new MethodInfoJNIComposer(methodInfo).compose())
                        .collect(Collectors.joining("\n")));

        final String mangledClassPath = JNIMangler.getMangledClassDescriptor(classInfo.getClazz());
        replacements.put(PLACEHOLDER_MANGLED_CLASSPATH, mangledClassPath);

        return replacements;
    }

    /**
     * @return All the native includes of the classInfo, ready to substitute the corresponding placeholder.
     */
    private String getNativeIncludes() {
        Set<String> includes = classInfo.getMetadata().getIncludes();
        return includes.stream()
                .map(include -> "#include " + (!include.startsWith("<") ? "\"" + include + "\"" : include))
                .collect(Collectors.joining("\n"));
    }

    /**
     * @return All the custom JNI code of the classInfo, ready to substitute the corresponding placeholder.
     */
    private String getCustomJNICode() {
        Set<String> customJNICodePaths = classInfo.getMetadata().getCustomJNICodePaths();

        return customJNICodePaths.stream()
                .map(ResourceUtils::load)
                .collect(Collectors.joining("\n"));
    }
}
