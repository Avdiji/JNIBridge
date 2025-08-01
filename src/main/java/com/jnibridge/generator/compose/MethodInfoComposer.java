package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.model.TypeInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for composing string representations of a {@link MethodInfo}.
 *
 * <p>
 * Implementations of this class are responsible for transforming
 * the metadata contained in a {@link MethodInfo} instance into a
 * specific output format, such as JNI declarations, JSON metadata, ...
 * </p>
 */
@Getter
@RequiredArgsConstructor
public abstract class MethodInfoComposer {

    @NonNull
    private final MethodInfo methodInfo;

    /**
     * Composes a string representation of the associated {@link MethodInfo}.
     *
     * @return the formatted string representation.
     */
    @NotNull
    public abstract String compose();

    /**
     * Returns a map of placeholder-to-value pairs used to replace
     * the placeholders defined in the type-mapping templates.
     *
     * @return a map of template placeholders to their resolved replacement values
     */
    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        replacements.put(TypeInfoComposer.PLACEHOLDER_JNI_TYPE, methodInfo.getReturnType().getJniType());


        return replacements;
    }

}
