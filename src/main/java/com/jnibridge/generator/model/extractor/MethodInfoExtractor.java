package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.mapping.MethodName;
import com.jnibridge.annotations.mapping.MethodNamespace;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.nativeaccess.IPointer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static com.jnibridge.generator.model.extractor.ClassInfoExtractor.extractClassCType;

/**
 * Utility class responsible for extracting a {@link MethodInfo} model from a Java {@link Method}.
 */
public class MethodInfoExtractor {

    /**
     * Constructor
     */
    private MethodInfoExtractor() { }

    /**
     * Extracts a {@link MethodInfo} object from a Java {@link Method}.
     *
     * @param method         the Java method to extract information from
     * @param classNamespace the namespace of the native class, the method resides in.
     * @return a fully populated {@link MethodInfo} model
     */
    @NotNull
    protected static MethodInfo extract(@NotNull final Method method, @NotNull final String classNamespace, @NotNull final Class<?> classToBeMapped) {
        Optional<MethodNamespace> namespaceOpt = Optional.ofNullable(method.getAnnotation(MethodNamespace.class));
        Optional<MethodName> nameOpt = Optional.ofNullable(method.getAnnotation(MethodName.class));

        final boolean isStatic = Modifier.isStatic(method.getModifiers());
        final BridgeClass bridgeClassAnnotation = method.getDeclaringClass().getAnnotation(BridgeClass.class);

        MethodInfo.MethodInfoBuilder methodBuilder = MethodInfo.builder()
                .method(method)

                .isStatic(isStatic)

                .namespace(namespaceOpt.isPresent() ? namespaceOpt.get().value() : classNamespace)
                .nativeName(nameOpt.isPresent() ? nameOpt.get().value() : method.getName())
                .jName(method.getName())

                .returnType(TypeInfoExtractor.extractReturnType(method, bridgeClassAnnotation))
                .params(TypeInfoExtractor.extractParamTypes(method, bridgeClassAnnotation));

        // in case the method is an instance method
        if (!isStatic && IPointer.class.isAssignableFrom(classToBeMapped)) {
            methodBuilder.selfType(TypeInfoExtractor.extractSelfType(classToBeMapped, extractClassCType(classToBeMapped)));
        }

        return methodBuilder.build();
    }

}
