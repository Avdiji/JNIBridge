package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Custom;
import com.jnibridge.annotations.modifiers.IgnoreNullcheck;
import com.jnibridge.exception.JniBridgeException;
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

    @NonNull
    private final MethodInfo methodInfo;

    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        addTemplateTypeReplacements(replacements);

        Optional.ofNullable(methodInfo.getSelfType()).ifPresent(selfType -> replacements.put(Placeholder.SELF_IN_MAPPING, new TypeInfoJNIComposer(selfType).compose()));

        replacements.put(Placeholder.PARAMS_IN_MAPPING, getParamInputMappings());
        replacements.put(Placeholder.RESULT_OUT_MAPPING, new TypeInfoJNIComposer(methodInfo.getReturnType()).compose());

        replacements.put(Placeholder.JNI_TYPE, methodInfo.getReturnType().getJniType());
        replacements.put(Placeholder.MANGLED_FUNC_NAME, JNIMangler.getMangledMethodDescriptor(methodInfo.getMethod()));

        replacements.put(Placeholder.JNI_PARAMS, getJNIFunctionParams());
        replacements.put(Placeholder.NULL_CHECK, getNullChecks());

        replacements.put(Placeholder.JNI_CLEANUP, getCleanupLogic());
        replacements.put(Placeholder.C_TYPE, methodInfo.getReturnType().getCType());
        replacements.put(Placeholder.RESULT_DECLARATION, getResultDeclaration());
        replacements.put(Placeholder.RETURN_CALL, methodInfo.getReturnType().getJniType().equals("void") ? "\t\t\treturn;" : "\t\t\treturn result;");

        Optional<Custom> custom = methodInfo.getReturnType().getAnnotation(Custom.class);
        replacements.put(Placeholder.FUNC_CALL, Composer.getReplacement(getNativeFunctionCall(), custom.map(Custom::functionCall).orElse(null)));
        replacements.put(Placeholder.FUNC_CALL_PARAMS, getNativeFunctionCallParams());

        return replacements;
    }

    private void addTemplateTypeReplacements(Map<String, String> replacements) {
        final Optional<Custom> customAnnotationOpt = methodInfo.getReturnType().getAnnotation(Custom.class);
        if (customAnnotationOpt.isPresent() && !customAnnotationOpt.get().bodyTemplatePath().isEmpty()) {

            Custom customAnnotation = customAnnotationOpt.get();
            if (customAnnotation.jTemplateArgumentTypes().length == 0) { return; }

            // check for any template argument types...
            final Class<?>[] jTemplateArgumentTypes = customAnnotation.jTemplateArgumentTypes();
            final String[] cTemplateTypes = customAnnotation.cTemplateArgumentTypes();
            if (jTemplateArgumentTypes.length != cTemplateTypes.length) {
                throw new JniBridgeException("The length of the C++ specific and Java specific template argument types must be equal.");
            }

            for (int i = 0; i < cTemplateTypes.length; ++i) {

                replacements.put(String.format("%s_%d", Placeholder.C_TEMPLATE_TYPE, i), cTemplateTypes[i]);
                replacements.put(String.format("%s_%d", Placeholder.C_TEMPLATE_TYPE_UNDERSCORE, i), cTemplateTypes[i].replace("::", "_"));

                final String jTemplateArgumentReplacement = Optional.ofNullable(jTemplateArgumentTypes[i])
                        .map(Class::getName)
                        .map(name -> name.replace(".", "/"))
                        .orElse("$INVALID MAPPING");
                replacements.put(String.format("%s_%d", Placeholder.JAVA_TEMPLATE_PATH, i), jTemplateArgumentReplacement);
            }
        }
    }


    /**
     * @return replacement for {@link Placeholder#JNI_CLEANUP}.
     */
    private String getCleanupLogic() {
        return methodInfo.getParams().stream()
                .map(TypeInfo::getCleanupLogic)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    /**
     * @return Replacement for the {@link Placeholder#RESULT_DECLARATION}
     */
    private String getResultDeclaration() {
        TypeInfo returnType = methodInfo.getReturnType();

        if (returnType.getJniType().equals("void")) { return ""; }
        return String.format("\t\t\t%s result = jnibridge::internal::jniDefaultReturn<%s>();", returnType.getJniType(), returnType.getJniType());


    }

    /**
     * @return A replacement for the null check placeholder.
     */
    private String getNullChecks() {
        final StringBuilder result = new StringBuilder();
        methodInfo.getParams().stream()
                .filter(p -> !p.hasAnnotation(IgnoreNullcheck.class))
                .filter(p -> !p.getType().isPrimitive())
                .forEach(p -> result.append("\t\t\tif (!")
                        .append(Placeholder.JNI_VAR)
                        .append(p.getId())
                        .append(") { jnibridge::internal::throwJniBridgeExceptionJava(env, \"passing Nullpointer.\"); ")
                        .append(methodInfo.getReturnType().getJniType().equals("void") ? "return" : "return result")
                        .append("; }\n")
                );

        // no null checks needed...
        if (result.length() == 0) { return ""; }
        return String.format("\t\t\t// CHECK NULLPTR\n%s", result);
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

        final StringBuilder result = new StringBuilder();
        params.stream()
                .map(param -> new TypeInfoJNIComposer(param).compose())
                .forEach(paramMapping -> result.append(paramMapping)
                        .append("\t\t\tjnibridge::internal::capturePendingJException(env, pendingJExceptions);\n\n")
                );
        result.append("\n\n\t\t\tif(!pendingJExceptions.empty()) { goto cleanup; }");

        return "\t\t\t// INPUT MAPPINGS\n" + result;
    }

}
