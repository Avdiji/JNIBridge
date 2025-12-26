package com.jnibridge.generator.compose.jni.helper;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.generator.compose.Placeholder;
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

    public static final String INTERNAL_FILENAME = "JniBridgeHandle.cpp";

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
        replacements.put(Placeholder.INTERNAL_INCLUDES, allIncludesStr);

        // add all custom JNI-code.
        final StringBuilder result = new StringBuilder();
        customJniCodePaths.stream().map(ResourceUtils::load).forEach(result::append);
        replacements.put(Placeholder.CUSTOM_JNI, result.toString());

        return replacements;
    }
}
