package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.Name;
import com.jnibridge.annotations.Namespace;
import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.nativeaccess.IPointer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static com.jnibridge.generator.model.extractor.ClassInfoExtractor.extractClassCType;

/**
 * Utility class responsible for extracting a {@link MethodInfo} model from a Java {@link Method}.
 */
public class MethodInfoExtractor {

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
        Optional<Namespace> namespaceOpt = Optional.ofNullable(method.getAnnotation(Namespace.class));
        Optional<Name> nameOpt = Optional.ofNullable(method.getAnnotation(Name.class));

        final boolean isStatic = Modifier.isStatic(method.getModifiers());

        MethodInfo.MethodInfoBuilder methodBuilder = MethodInfo.builder()
                .method(method)

                .isStatic(isStatic)
                .isDealloc(Arrays.stream(method.getDeclaredAnnotations()).anyMatch(annotation -> annotation instanceof Deallocate))
                .isAlloc(Arrays.stream(method.getDeclaredAnnotations()).anyMatch(annotation -> annotation instanceof Allocate))

                .namespace(namespaceOpt.isPresent() ? namespaceOpt.get().value() : classNamespace)
                .nativeName(nameOpt.isPresent() ? nameOpt.get().value() : method.getName())
                .jName(method.getName())

                .returnType(TypeInfoExtractor.extractReturnType(method))
                .params(TypeInfoExtractor.extractParamTypes(method));

        // in case the method is a instance method
        if (!isStatic && IPointer.class.isAssignableFrom(classToBeMapped)) {
            methodBuilder.selfType(TypeInfoExtractor.extractSelfType(classToBeMapped, extractClassCType(classToBeMapped)));
        }

        return methodBuilder.build();
    }

}
