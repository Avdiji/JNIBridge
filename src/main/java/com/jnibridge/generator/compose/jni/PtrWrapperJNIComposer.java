package com.jnibridge.generator.compose.jni;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PtrWrapperJNIComposer implements Composer {

    public static final String INTERNAL_FILENAME = "JniBridgeHandle.internal.hpp";

    private final Collection<String> includes;


    public PtrWrapperJNIComposer(@NotNull final Collection<String> includes) {
        this.includes = includes;
    }

    @Override
    public String compose() {
        String internals = ResourceUtils.load("com/jnibridge/internals/handle/" + INTERNAL_FILENAME);
        return TemplateUtils.substitute(internals, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        final String allIncludesStr = includes.stream()
                .map(include -> String.format("#include \"%s\"", include))
                .collect(Collectors.joining("\n"));
        replacements.put("allIncludes", allIncludesStr);

        return replacements;
    }
}
