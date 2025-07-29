package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.Mapping;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.mapper.TypeMapper;
import com.jnibridge.mapper.TypeMappingRegistry;
import com.jnibridge.utils.ResourceUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Utility class responsible for extracting {@link TypeInfo} from a method.
 */
public class TypeInfoExtractor {

    private TypeInfoExtractor() { }

    /**
     * Extracts a {@link TypeInfo} representation from a method's return type.
     *
     * @param method the method whose return type should be processed
     * @return a {@link TypeInfo} describing the return type
     * @throws IllegalArgumentException if no valid {@link TypeMapper} is registered or annotated for the return type
     */
    protected static TypeInfo extractReturnType(@NotNull final Method method) {
        return extract(method.getReturnType(), method.getAnnotatedReturnType().getAnnotations());
    }

    /**
     * Extracts a list of {@link TypeInfo} objects for each parameter of the given method.
     *
     * @param method the method whose parameters should be processed
     * @return a list of {@link TypeInfo} objects representing each parameter
     * @throws IllegalArgumentException if a parameter's type has no valid {@link TypeMapper} registered or annotated
     */
    protected static List<TypeInfo> extractParamTypes(@NotNull final Method method) {
        List<TypeInfo> result = new LinkedList<>();

        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < method.getParameterCount(); ++i) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] paramAnnotations = parameterAnnotations[i];
            result.add(extract(paramType, paramAnnotations));
        }
        return result;
    }

    /**
     * Internal helper that builds a {@link TypeInfo} object from a given type and its annotations.
     *
     * @param type        the Java class to resolve
     * @param annotations annotations present on the type (param or return)
     * @return a fully populated {@link TypeInfo} object
     * @throws IllegalArgumentException if no valid {@link Mapping} is found for the resolved {@link TypeMapper}
     */
    private static TypeInfo extract(@NotNull final Class<?> type, final Annotation[] annotations) {
        Mapping mapping = validateTypeMapping(type);
        List<Annotation> annotationList = Arrays.stream(annotations).collect(Collectors.toList());

        return TypeInfo.builder()
                .type(type)
                .annotations(annotationList)
                .cType(mapping.cType())
                .inMapping(ResourceUtils.load(mapping.inPath()))
                .outMapping(ResourceUtils.load(mapping.outPath()))
                .build();
    }

    /**
     * Resolves the {@link Mapping} annotation from the registered {@link TypeMapper} for the given type.
     * Ensures that the mapping configuration exists and is valid.
     *
     * @param type the Java type to look up in the registry
     * @return the associated {@link Mapping} annotation
     * @throws IllegalArgumentException if no {@link TypeMapper} is registered or if the mapper is missing {@link Mapping}
     */
    @NotNull
    private static Mapping validateTypeMapping(@NotNull final Class<?> type) {
        Class<? extends TypeMapper> mapper = TypeMappingRegistry.getMapperFor(type);

        if (mapper == null) {
            throw new IllegalArgumentException(String.format("No mapper for type '%s' has been registered.", type.getSimpleName()));
        }

        Mapping mapping = mapper.getAnnotation(Mapping.class);
        if (mapping == null) {
            throw new IllegalArgumentException(String.format("Mapper for type '%s' must be annotated properly.", type));
        }

        return mapping;
    }

}
