package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.utils.JNIMangler;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public abstract class MethodInfoComposer implements Composer{

    // MethodInfo - related Placeholders...
    public static final String PLACEHOLDER_MANGLED_FUNCTION_NAME = "mangledFuncName";
    public static final String PLACEHOLDER_JNI_PARAMS = "jniParams";
    public static final String PLACEHOLDER_FUNCTION_CALL = "functionCall";

    @NonNull
    private final MethodInfo methodInfo;

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
        replacements.put(PLACEHOLDER_MANGLED_FUNCTION_NAME, JNIMangler.getMangledMethodDescriptor(methodInfo.getMethod()));
        replacements.put(PLACEHOLDER_JNI_PARAMS, getParamPlaceholders(true));
        replacements.put(PLACEHOLDER_FUNCTION_CALL, String.format("%s(%s)", methodInfo.getNativeName(), getParamPlaceholders(false)));

        return replacements;
    }

    /**
     * Builds a comma-prefixed list of JNI parameter placeholders for this method.
     *
     * @param withType whether to include JNI types in the placeholders
     * @return a comma-prefixed string of JNI argument placeholders, or an empty string if the method has no parameters
     *
     * <p>
     * @throws IllegalStateException if any {@link TypeInfo} used as a parameter is missing its id
     */
    private String getParamPlaceholders(final boolean withType) {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        String result = params.stream().map(typeInfo -> {
            String id = typeInfo.getId();
            if (id == null) {
                throw new IllegalStateException("TypeInfo's that act as parameter types must have a id!");
            }

            String typePrefix = withType ? typeInfo.getJniType() + " " : "";
            return typePrefix + TypeInfoComposer.PLACEHOLDER_JNI_VAR + id;
        }).collect(Collectors.joining(", "));

        return ", " + result;
    }

}
