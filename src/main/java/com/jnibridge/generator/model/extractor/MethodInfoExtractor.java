package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.Name;
import com.jnibridge.annotations.Namespace;
import com.jnibridge.generator.model.MethodInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Utility class responsible for extracting a {@link MethodInfo} model from a Java {@link Method}.
 */
public class MethodInfoExtractor {

    private MethodInfoExtractor() { }

    /**
     * Extracts a {@link MethodInfo} object from a Java {@link Method}.
     *
     * @param method the Java method to extract information from
     * @return a fully populated {@link MethodInfo} model
     */
    protected static MethodInfo extract(@NotNull final Method method) {
        Optional<Namespace> namespaceOpt = Optional.ofNullable(method.getAnnotation(Namespace.class));
        Optional<Name> nameOpt = Optional.ofNullable(method.getAnnotation(Name.class));

        return MethodInfo.builder()
                .method(method)

                .namespace(namespaceOpt.isPresent() ? namespaceOpt.get().nativeNamespace() : "")
                .nativeName(nameOpt.isPresent() ? nameOpt.get().nativeName() : method.getName())
                .jName(method.getName())

                .returnType(TypeInfoExtractor.extractReturnType(method))
                .params(TypeInfoExtractor.extractParamTypes(method))
                .build();
    }

}
