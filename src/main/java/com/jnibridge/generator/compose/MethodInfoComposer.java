package com.jnibridge.generator.compose;

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
 * Composes string representations of {@link MethodInfo} objects.
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

    /**
     * Generates a comma-separated list of JNI function parameters, each represented as:
     *
     * @return a string suitable for use in JNI function signatures, or an empty string if no params exist
     */
    private String getJNIFunctionParams() {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        String result = params.stream().map(typeInfo -> {

            String id = Objects.requireNonNull(typeInfo.getId(), "TypeInfo's that act as parameter types must have a id!");
            return String.format("%s %s%s", typeInfo.getJniType(), TypeInfoComposer.PLACEHOLDER_JNI_VAR, id);

        }).collect(Collectors.joining(", "));

        return ", " + result;
    }

    /**
     * Generates the list of native arguments for the function call, mapping JNI variables to their corresponding C variables
     *
     * @return a string containing the argument list for the native function call
     */
    private String getNativeFunctionCallArgs() {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        return params.stream().map(typeInfo -> {

            String id = Objects.requireNonNull(typeInfo.getId(), "TypeInfo's that act as parameter types must have a id!");
            return String.format("%s%s", TypeInfoComposer.PLACEHOLDER_C_VAR, id);

        }).collect(Collectors.joining(", "));

    }

    /**
     * Composes the code snippets required to map each JNI input parameter into its corresponding native type representation.
     *
     * @return concatenated parameter input mapping code snippets, or empty string if no params exist
     */
    private String getParamInputMappings() {
        List<TypeInfo> params = methodInfo.getParams();
        if (params.isEmpty()) { return ""; }

        return params.stream()
                .map(param -> new TypeInfoJNIComposer(param).compose())
                .collect(Collectors.joining());
    }

}
