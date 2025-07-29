package com.jnibridge.generator.model.extractor;

import com.jnibridge.generator.model.MethodInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

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
    public static MethodInfo extract(@NotNull final Method method) {
        return MethodInfo.builder()
                .methodName(method.getName())
                .returnType(TypeInfoExtractor.extractReturnType(method))
                .params(TypeInfoExtractor.extractParamTypes(method))
                .build();
    }

}
