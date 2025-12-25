package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Custom;
import com.jnibridge.generator.compose.jni.TypeInfoJNIComposer;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.utils.JNIMangler;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Composes string representations of {@link MethodInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class MethodInfoComposer implements Composer {

    // MethodInfo - related Placeholders...


    @NonNull
    private final MethodInfo methodInfo;

    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        Optional.ofNullable(methodInfo.getSelfType()).ifPresent(selfType -> replacements.put(Placeholder.SELF_IN_MAPPING, new TypeInfoJNIComposer(selfType).compose()));

        replacements.put(Placeholder.PARAMS_IN_MAPPING, getParamInputMappings());
        replacements.put(Placeholder.RESULT_OUT_MAPPING, new TypeInfoJNIComposer(methodInfo.getReturnType()).compose());

        replacements.put(Placeholder.JNI_TYPE, methodInfo.getReturnType().getJniType());
        replacements.put(Placeholder.MANGLED_FUNC_NAME, JNIMangler.getMangledMethodDescriptor(methodInfo.getMethod()));

        replacements.put(Placeholder.JNI_PARAMS, getJNIFunctionParams());

        Optional<Custom> custom = methodInfo.getReturnType().getAnnotation(Custom.class);
        replacements.put(Placeholder.FUNC_CALL, Composer.getReplacement(getNativeFunctionCall(), custom.map(Custom::functionCall).orElse(null)));
        replacements.put(Placeholder.FUNC_CALL_PARAMS, getNativeFunctionCallParams());

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
            return String.format("%s %s%s", typeInfo.getJniType(), Placeholder.JNI_VAR, id);

        }).collect(Collectors.joining(", "));

        return ", " + result;
    }

    /**
     * Builds the native function call expression.
     * Example (instance):  cSelf->Namespace::foo(a, b)
     * Example (static):    Namespace::foo(a, b)
     */
    private String getNativeFunctionCall() {
        final boolean isStatic = methodInfo.isStatic();
        final String ns = methodInfo.getNamespace();

        final String qualifier = (isStatic && !ns.isEmpty()) ? (ns + "::") : "";
        final String receiver = isStatic ? "" : ("cself" + "->");
        final String params = getNativeFunctionCallParams();

        return receiver + qualifier + methodInfo.getNativeName() + "(" + params + ")";
    }

    /**
     * Builds the comma-prefixed JNI parameters (e.g., ", jint jniVar0, jstring jniVar1"),
     * or an empty string if there are no parameters.
     */
    private String getNativeFunctionCallParams() {
        List<TypeInfo> params = methodInfo.getParams();
        return params.stream().map(typeInfo -> {

            String id = Objects.requireNonNull(typeInfo.getId(), "TypeInfo's that act as parameter types must have a id!");
            return String.format("%s%s", Placeholder.C_VAR, id);

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
