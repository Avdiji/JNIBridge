package com.jnibridge.generator.compose.jni.helper;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Compose the JniBridgeHandle - helper file.
 */
@RequiredArgsConstructor
public class JniBridgeHandleComposer implements Composer {

    public static final String INTERNAL_FILENAME = "JniBridgeHandle.hpp";

    private static final String PLACEHOLDER_CUSTOM_JNI_CODE = "customJNICode";
    private static final String PLACEHOLDER_INCLUDES = "allIncludes";

    private final Collection<String> includes;
    private final Collection<String> customJniCodePaths;

    @Override
    public String compose() {
        String internals = ResourceUtils.load("com/jnibridge/internals/handle/" + INTERNAL_FILENAME);
        return TemplateUtils.substitute(internals, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        // add all includes.
        final String allIncludesStr = includes.stream()
                .map(include -> String.format("#include \"%s\"", include))
                .collect(Collectors.joining("\n"));
        replacements.put(PLACEHOLDER_INCLUDES, allIncludesStr);

        // add all custom JNI-code.
        final StringBuilder result = new StringBuilder();
        customJniCodePaths.stream().map(ResourceUtils::load).forEach(result::append);
        replacements.put(PLACEHOLDER_CUSTOM_JNI_CODE, result.toString());

        return replacements;
    }
}
