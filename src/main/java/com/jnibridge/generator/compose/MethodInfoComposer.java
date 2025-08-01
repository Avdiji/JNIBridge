package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.generator.compose.jni.TypeInfoJNIComposer;
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
import java.util.Objects;
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
public abstract class MethodInfoComposer implements Composer {

    // MethodInfo - related Placeholders...
    public static final String PLACEHOLDER_MANGLED_FUNCTION_NAME = "mangledFuncName";
    public static final String PLACEHOLDER_JNI_PARAMS = "jniParams";
    public static final String PLACEHOLDER_FUNCTION_CALL = "functionCall";

    public static final String PLACEHOLDER_CALLING_OBJ_IN_MAPPING = "callingObjInMapping";
    public static final String PLACEHOLDER_PARAMS_IN_MAPPING = "paramInMapping";
    public static final String PLACEHOLDER_RESULT_OUT_MAPPING = "resultOutMapping";


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

        replacements.put(PLACEHOLDER_PARAMS_IN_MAPPING, getParamInputMappings());
        replacements.put(PLACEHOLDER_RESULT_OUT_MAPPING, new TypeInfoJNIComposer(methodInfo.getReturnType()).compose());

        replacements.put(TypeInfoComposer.PLACEHOLDER_JNI_TYPE, methodInfo.getReturnType().getJniType());
        replacements.put(PLACEHOLDER_MANGLED_FUNCTION_NAME, JNIMangler.getMangledMethodDescriptor(methodInfo.getMethod()));

        replacements.put(PLACEHOLDER_JNI_PARAMS, getJNIFunctionParams());

        replacements.put(PLACEHOLDER_FUNCTION_CALL, String.format("%s(%s)", methodInfo.getNativeName(), getNativeFunctionCallArgs()));

        return replacements;
    }


    private String getJNIFunctionParams() {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        String result = params.stream().map(typeInfo -> {

            String id = Objects.requireNonNull(typeInfo.getId(), "TypeInfo's that act as parameter types must have a id!");
            return String.format("%s %s%s", typeInfo.getJniType(), TypeInfoComposer.PLACEHOLDER_JNI_VAR, id);

        }).collect(Collectors.joining(", "));

        return ", " + result;
    }

    private String getNativeFunctionCallArgs() {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        return params.stream().map(typeInfo -> {

            String id = Objects.requireNonNull(typeInfo.getId(), "TypeInfo's that act as parameter types must have a id!");
            return String.format("%s%s", TypeInfoComposer.PLACEHOLDER_C_VAR, id);

        }).collect(Collectors.joining(", "));

    }


    private String getParamInputMappings() {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        return params.stream()
                .map(param -> new TypeInfoJNIComposer(param).compose())
                .collect(Collectors.joining("\t"));
    }

}
