package com.jnibridge.generator.model.extractor;

import com.jnibridge.JniBridgeRegistry;
import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.lifecycle.Ptr;
import com.jnibridge.annotations.lifecycle.Ref;
import com.jnibridge.annotations.lifecycle.Shared;
import com.jnibridge.annotations.lifecycle.Unique;
import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.annotations.mapping.UseMapping;
import com.jnibridge.annotations.modifiers.Custom;
import com.jnibridge.exception.JniBridgeException;
import com.jnibridge.generator.compose.Placeholder;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.mapper.TypeMapper;
import com.jnibridge.nativeaccess.IPointer;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
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

    /**
     * Constructor.
     */
    private TypeInfoExtractor() { }

    /**
     * Extract the type of the calling instance.
     *
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
                .outMapping("") // not needed...
                .isInvoker(true)
                .build();
    }

    /**
     * Extracts a {@link TypeInfo} representation from a method's return type.
     *
     * @param method the method whose return type should be processed
     * @return a {@link TypeInfo} describing the return type
     * @throws IllegalArgumentException if no valid {@link TypeMapper} is registered or annotated for the return type
     */
    @NotNull
    protected static TypeInfo extractReturnType(@NotNull final Method method) {
        return extract(method.getReturnType(), null, method.getDeclaredAnnotations(), extractClassWideMappings(method));
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

        // fetch all parameter specific annotations...
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // iterate through all params of the passed method
        for (int i = 0; i < method.getParameterCount(); ++i) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] paramAnnotations = parameterAnnotations[i];
            result.add(extract(paramType, "" + i, paramAnnotations, extractClassWideMappings(method)));
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
    @NotNull
    private static TypeInfo extract(@NotNull final Class<?> type, @Nullable final String id, final Annotation[] annotations, @NotNull final BridgeClass.MappingEntry[] classWideMappings) {
        List<Annotation> annotationList = Arrays.stream(annotations).collect(Collectors.toList());

        // Extract enum types...
        final BridgeClass bridgeClassAnnotation = type.getAnnotation(BridgeClass.class);
        if (bridgeClassAnnotation != null && bridgeClassAnnotation.isEnum()) {
            return extractEnumType(type, id, annotationList);
        }

        // Extract from an IPointer instance...
        if (IPointer.class.isAssignableFrom(type)) {
            return extractIPointerType(type, id, annotationList);
        }

        // Extract types that are mapped via TypeMappers...
        return extractFromTypeMapper(type, id, annotationList, classWideMappings);
    }

    /**
     * Extract the {@link TypeInfo} for enum-types.
     *
     * @param type        The enum type to extract the info from.
     * @param id          A unique identifier.
     * @param annotations The annotations of the corresponding type.
     * @return An instance of {@link TypeInfo}.
     */
    private static TypeInfo extractEnumType(@NotNull final Class<?> type, @Nullable final String id, final List<Annotation> annotations) {

        return TypeInfo.builder()
                .type(type)
                .id(id)
                .annotations(annotations)
                .cType(ClassInfoExtractor.extractClassCType(type))
                .jniType("jobject")
                .isInvoker(false)
                .inMapping(ResourceUtils.load("com/jnibridge/mappings/bridged_classes/enum/jnibridge.enum.in.mapping"))
                .outMapping(ResourceUtils.load("com/jnibridge/mappings/bridged_classes/enum/jnibridge.enum.out.mapping"))
                .build();
    }

    /**
     * Extract the TypeInfo from a class, implementing the {@link IPointer} interface.
     *
     * @param type        The class-type of the type to extract the <code>InfoType</code> from.
     * @param id          A unique identifier (used for the JNI-Mapping process).
     * @param annotations All the annotations of the corresponding type.
     * @return An instance of {@link TypeInfo} from the passed parameter.
     */
    private static TypeInfo extractIPointerType(@NotNull final Class<?> type, @Nullable final String id, final List<Annotation> annotations) {
        final TypeInfo result = TypeInfo.builder()
                .type(type)
                .id(id)
                .annotations(annotations)
                .cType(ClassInfoExtractor.extractClassCType(type))
                .jniType("jobject")
                .isInvoker(false)
                .build();

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

        // Mapping for uniquePtr...
        Optional<Unique> uniqueOpt = result.getAnnotation(Unique.class);
        uniqueOpt.ifPresent(unique -> {
            inMappingTemplatePath.setLength(0);
            outMappingTemplatePath.setLength(0);
            inMappingTemplatePath.append(unique.inMapping());
            outMappingTemplatePath.append(unique.outMapping());
        });

        // Custom Mappings...
        Optional<Custom> customOpt = result.getAnnotation(Custom.class);
        customOpt.ifPresent(custom -> {
            String customInMapping = custom.inMappingTemplatePath();
            String customOutMapping = custom.outMappingTemplatePath();

            if (!customInMapping.isEmpty()) {
                inMappingTemplatePath.setLength(0);
                inMappingTemplatePath.append(customInMapping);
            }

            if (!customOutMapping.isEmpty()) {
                outMappingTemplatePath.setLength(0);
                outMappingTemplatePath.append(customOutMapping);
            }
        });

        result.setInMapping(ResourceUtils.load(inMappingTemplatePath.toString()));
        result.setOutMapping(ResourceUtils.load(outMappingTemplatePath.toString()));
        return result;
    }

    /**
     * Extract an instance of {@link TypeInfo} from a registered {@link TypeMapper}.
     *
     * @param type              The type to be mapped.
     * @param id                The id of the resulting typeInfo.
     * @param annotations       All annotations of the parameter/type to be mapped.
     * @param classWideMappings Mappings that have been specified on the method-invoking class.
     * @return An instance of {@link TypeInfo}, composed by the information deduces from {@link TypeMapper} and {@link Mapping}.
     */
    private static TypeInfo extractFromTypeMapper(@NotNull final Class<?> type, @Nullable final String id, @NotNull final List<Annotation> annotations, @NotNull final BridgeClass.MappingEntry[] classWideMappings) {
        // check whether the param/returnValue is using the 'UseMapping' annotation.
        Mapping paramSpecificMapping = annotations.stream()
                .filter(annotation -> annotation instanceof UseMapping)
                .map(annotation -> ((UseMapping) annotation).value())
                .map(mapper -> validateMapper(mapper, type.getSimpleName()))
                .findFirst()
                .orElse(null);

        // check whether the param/returnValue is using a class-wide defined mapper (if nothing else has been specified).
        if (paramSpecificMapping == null) {
            paramSpecificMapping = Arrays.stream(classWideMappings)
                    .filter(entry -> entry.type().equals(type))
                    .map(BridgeClass.MappingEntry::mapper)
                    .filter(Objects::nonNull)
                    .map(mapper -> validateMapper(mapper, type.getSimpleName()))
                    .findFirst()
                    .orElse(null);
        }

        // use globally registered mappers if nothing else has been specified.
        if (paramSpecificMapping == null) {
            paramSpecificMapping = validateMapper(JniBridgeRegistry.getMapperForType(type), type.getSimpleName());
        }

        // TODO this should be its own composer...
        // extract the cleanup logic
        final String cleanupPath = paramSpecificMapping.cleanupPath();
        String cleanupLogic = "";
        if (!cleanupPath.isEmpty()) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put(Placeholder.ID, id);
            replacements.put(Placeholder.C_TYPE, paramSpecificMapping.cType());
            replacements.put(Placeholder.C_VAR, Placeholder.C_VAR + id);
            replacements.put(Placeholder.JNI_TYPE, paramSpecificMapping.jniType());
            replacements.put(Placeholder.JNI_VAR, Placeholder.JNI_VAR + id);

            final String cleanupTemplate = ResourceUtils.load(cleanupPath);
            cleanupLogic = TemplateUtils.substitute(cleanupTemplate, replacements, true);
        }

        // check for any template argument types...
        final Class<?>[] jTemplateArgumentTypes = paramSpecificMapping.jTemplateArgumentTypes();
        final String[] cTemplateTypes = paramSpecificMapping.cTemplateArgumentTypes();
        if (jTemplateArgumentTypes.length != cTemplateTypes.length) {
            throw new JniBridgeException("The length of the C++ specific and Java specific template argument types must be equal.");
        }

        // create a new TypeInfo
        return TypeInfo.builder()
                .type(type)
                .id(id)
                .annotations(annotations)
                .cType(paramSpecificMapping.cType())
                .jniType(paramSpecificMapping.jniType())
                .cTemplateArgumentTypes(Arrays.stream(cTemplateTypes).collect(Collectors.toCollection(LinkedList::new)))
                .javaTemplateArgumentTypes(Arrays.stream(jTemplateArgumentTypes).collect(Collectors.toCollection(LinkedList::new)))
                .inMapping(ResourceUtils.load(paramSpecificMapping.inPath()))
                .outMapping(ResourceUtils.load(paramSpecificMapping.outPath()))
                .isInvoker(false)
                .cleanupLogic(cleanupLogic)
                .build();
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
        // check whether a mapper for the type exists.
        if (mapper == null) {
            throw new JniBridgeException(String.format("Mapper for type '%s' has not been registered.", typename));
        }

        // make sure the mapper has been annotated properly.
        Mapping mapping = mapper.getAnnotation(Mapping.class);
        if (mapping == null) {
            throw new JniBridgeException(String.format("Mapper '%s' must be annotated with 'Mapping'.", mapper.getSimpleName()));
        }
        return mapping;
    }

    /**
     * @param method The method which resides in the class to extract the mappings from.
     * @return An array of {@link com.jnibridge.annotations.BridgeClass.MappingEntry}, that defines class-wide mappings.
     */
    private static BridgeClass.MappingEntry[] extractClassWideMappings(@NotNull final Method method) {
        final Optional<BridgeClass> bridgeClassAnnotation = Optional.ofNullable(method.getDeclaringClass().getAnnotation(BridgeClass.class));
        return bridgeClassAnnotation.map(BridgeClass::typeMappers).orElse(new BridgeClass.MappingEntry[]{});
    }

}
