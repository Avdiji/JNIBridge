package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.lifecycle.Ptr;
import com.jnibridge.annotations.lifecycle.Ref;
import com.jnibridge.annotations.lifecycle.Shared;
import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.annotations.typemapping.UseMapping;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.mapper.TypeMapper;
import com.jnibridge.mapper.GlobalMapperRegistry;
import com.jnibridge.nativeaccess.IPointer;
import com.jnibridge.utils.ResourceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
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
    @NotNull
    protected static TypeInfo extractReturnType(@NotNull final Method method) {
        return extract(method.getReturnType(), null, method.getDeclaredAnnotations());
    }

    /**
     * Extracts a list of {@link TypeInfo} objects for each parameter of the given method.
     *
     * @param method the method whose parameters should be processed
     * @return a list of {@link TypeInfo} objects representing each parameter
     * @throws IllegalArgumentException if a parameter's type has no valid {@link TypeMapper} registered or annotated
     */
    @NotNull
    protected static List<TypeInfo> extractParamTypes(@NotNull final Method method) {
        List<TypeInfo> result = new LinkedList<>();

        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < method.getParameterCount(); ++i) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] paramAnnotations = parameterAnnotations[i];
            result.add(extract(paramType, "" + i, paramAnnotations));
        }
        return result;
    }

    /**
     * @param clazz The class to be mapped.
     * @param cType The CType of the class to be mapped.
     * @return The TypeInfo, which maps the calling type to C++.
     */
    protected static TypeInfo extractSelfType(@NotNull final Class<?> clazz, @NotNull final String cType) {
        return TypeInfo.builder()
                .type(clazz)
                .id(null)
                .annotations(new ArrayList<>())
                .cType(cType)
                .jniType("jobject")
                .inMapping(ResourceUtils.load("com/jnibridge/mappings/bridged_classes/raw/jnibridge.ptr.in.mapping"))
                .outMapping("")
                .isSelf(true)
                .build();
    }

    /**
     * Internal helper that builds a {@link TypeInfo} object from a given type and its annotations.
     *
     * @param type        the Java class to resolve
     * @param annotations annotations present on the type (param or return)
     * @return a fully populated {@link TypeInfo} object
     * @throws IllegalArgumentException if no valid {@link Mapping} is found for the resolved {@link TypeMapper}
     */
    @NotNull
    private static TypeInfo extract(@NotNull final Class<?> type, @Nullable final String id, final Annotation[] annotations) {
        List<Annotation> annotationList = Arrays.stream(annotations).collect(Collectors.toList());

        // check whether the param/returnValue is using a specific mapper
        Mapping paramSpecificMapping = annotationList.stream()
                .filter(annotation -> annotation instanceof UseMapping)
                .map(annotation -> ((UseMapping) annotation).value())
                .map(mapper -> validateMapper(mapper, type.getSimpleName()))
                .findFirst()
                .orElse(null);

        // extract the IPointer-Type
        if (IPointer.class.isAssignableFrom(type)) {
            return extractIPointerType(type, id, annotationList);

            // use the mapping from the global registry
        } else if (paramSpecificMapping == null) {
            paramSpecificMapping = validateMapper(GlobalMapperRegistry.getMapperFor(type), type.getSimpleName());
        }

        // create a new TypeInfo
        return TypeInfo.builder()
                .type(type)
                .id(id)
                .annotations(annotationList)
                .cType(paramSpecificMapping.cType())
                .jniType(paramSpecificMapping.jniType())
                .inMapping(ResourceUtils.load(paramSpecificMapping.inPath()))
                .outMapping(ResourceUtils.load(paramSpecificMapping.outPath()))
                .isSelf(false)
                .build();
    }


    private static TypeInfo extractIPointerType(@NotNull final Class<?> type, @Nullable final String id, final List<Annotation> annotations) {
        final String cType = ClassInfoExtractor.extractClassCType(type);
        final String jniType = "jobject";

        final TypeInfo result = TypeInfo.builder()
                .type(type)
                .id(id)
                .annotations(annotations)
                .cType(cType)
                .jniType(jniType)
                .isSelf(false)
                .build();

        // TODO might have to differentiate between sharedRef, UniqueRef
        // Default mappings (if nothing has been specified -> map by value)
        final StringBuilder inMappingTemplatePath = new StringBuilder("com/jnibridge/mappings/bridged_classes/raw/jnibridge.val.in.mapping");
        final StringBuilder outMappingTemplatePath = new StringBuilder("com/jnibridge/mappings/bridged_classes/raw/jnibridge.val.out.mapping");

        // Mapping for ptr
        Optional<Ptr> ptrOpt = result.getAnnotation(Ptr.class);
        ptrOpt.ifPresent(ptr -> {
            inMappingTemplatePath.setLength(0);
            outMappingTemplatePath.setLength(0);
            inMappingTemplatePath.append(ptr.inMapping());
            outMappingTemplatePath.append(ptr.outMapping());
        });

        // Mapping for refs
        Optional<Ref> refOpt = result.getAnnotation(Ref.class);
        refOpt.ifPresent(ref -> {
            inMappingTemplatePath.setLength(0);
            outMappingTemplatePath.setLength(0);
            inMappingTemplatePath.append(ref.inMapping());
            outMappingTemplatePath.append(ref.outMapping());
        });

        // Mapping for sharedPtr...
        Optional<Shared> sharedOpt = result.getAnnotation(Shared.class);
        sharedOpt.ifPresent(shared -> {
            inMappingTemplatePath.setLength(0);
            outMappingTemplatePath.setLength(0);
            inMappingTemplatePath.append(shared.inMapping());
            outMappingTemplatePath.append(shared.outMapping());
        });

        // TODO support unique mapping...

        result.setInMapping(ResourceUtils.load(inMappingTemplatePath.toString()));
        result.setOutMapping(ResourceUtils.load(outMappingTemplatePath.toString()));
        return result;
    }

    /**
     * Validates that a given {@link TypeMapper} class is not {@code null} and is properly annotated with {@link Mapping}.
     * <p>
     *
     * @param mapper   the {@link TypeMapper} class to validate; may be {@code null}
     * @param typename the name of the type to be mapped.
     * @return the resolved {@link Mapping} annotation from the given class
     * @throws IllegalArgumentException if the mapper is {@code null} or not annotated with {@link Mapping}
     */
    @NotNull
    private static Mapping validateMapper(@Nullable final Class<? extends TypeMapper> mapper, @NotNull final String typename) {
        if (mapper == null) {
            throw new IllegalArgumentException(String.format("Mapper for type '%s' has not been registered properly.", typename));
        }

        Mapping mapping = mapper.getAnnotation(Mapping.class);
        if (mapping == null) {
            throw new IllegalArgumentException(String.format("Mapper '%s' must be annotated properly.", mapper.getSimpleName()));
        }
        return mapping;
    }
}
