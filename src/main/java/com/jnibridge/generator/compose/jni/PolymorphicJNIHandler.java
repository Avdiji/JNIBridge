package com.jnibridge.generator.compose.jni;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.generator.compose.TypeInfoComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PolymorphicJNIHandler implements Composer {

    public static final String INTERNAL_FILENAME = "JniPolymorphicHandler.internal.jni.hpp";

    private static final String PLACEHOLDER_INTERNAL_WRAPPER_INCLUDE = "internalPtrWrapperPath";
    private static final String PLACEHOLDER_HELPER_FUNCTIONS = "helperFunctions";


    private final Collection<ClassInfo> instancesToMap;

    public PolymorphicJNIHandler(@NonNull final Collection<ClassInfo> classesToMap) { this.instancesToMap = classesToMap; }

    @Override
    public String compose() {
        String template = ResourceUtils.load("com/jnibridge/internals/polymorphicHandler/" + INTERNAL_FILENAME);
        return TemplateUtils.substitute(template, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        replacements.put(PLACEHOLDER_INTERNAL_WRAPPER_INCLUDE, PtrWrapperJNIComposer.INTERNAL_FILENAME);
        replacements.put(PLACEHOLDER_HELPER_FUNCTIONS, getHelperFunctions());

        return replacements;
    }

    private String getHelperFunctions() {
        return instancesToMap.stream()
                .filter(classInfo -> !classInfo.getSubclasses().isEmpty())
                .map(classInfo -> new PolymorphicJNIHandlerFuncComposer(classInfo).compose())
                .collect(Collectors.joining("\n\n"));
    }


    private static class PolymorphicJNIHandlerFuncComposer implements Composer {

        private static final String PLACEHOLDER_C_TYPE_UNDERSCORE = "cTypeUnderscore";
        private static final String PLACEHOLDER_LONG_TO_WRAPPER_BODY = "longToWrapperBody";
        private static final String PLACEHOLDER_WRAPPER_TO_CLASS_NAME_BODY = "wrapperToJClassNameBody";

        private final ClassInfo classInfo;

        public PolymorphicJNIHandlerFuncComposer(@NonNull final ClassInfo classInfo) { this.classInfo = classInfo; }

        @Override
        public String compose() {
            String template = ResourceUtils.load("com/jnibridge/internals/polymorphicHandler/JniPolyMorphicHandlerFunc.internal.jni.hpp");
            return TemplateUtils.substitute(template, getReplacements());
        }

        @Override
        public @NotNull Map<String, String> getReplacements() {
            Map<String, String> replacements = new HashMap<>();

            replacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE, ClassInfoExtractor.extractClassCType(classInfo.getClazz()));
            replacements.put(PLACEHOLDER_C_TYPE_UNDERSCORE, ClassInfoExtractor.extractClassCType(classInfo.getClazz()).replace("::", "_"));

            replacements.put(PLACEHOLDER_LONG_TO_WRAPPER_BODY, generateLongToWrapperBody());
            replacements.put(PLACEHOLDER_WRAPPER_TO_CLASS_NAME_BODY, "");

            return replacements;
        }


        private String generateLongToWrapperBody() {
            final String cType = ClassInfoExtractor.extractClassCType(classInfo.getClazz());
            StringBuilder result = new StringBuilder("\t\tauto *base = reinterpret_cast<jnibridge::internal::JniBridgePtrWrapperBase*>(nativeHandle);");
            boolean firstIteration = true;

            for (final ClassInfo subclass : classInfo.getSubclasses()) {

                final String subclassCType = ClassInfoExtractor.extractClassCType(subclass.getClazz());

                result.append("\n\n\t\t");
                result.append(firstIteration ? "if " : "else if ");
                firstIteration = false;

                result.append(String.format("(auto* actualType = dynamic_cast<jnibridge::internal::JniBridgePtrWrapper<%s>*>(base)) {", subclassCType));
                result.append(String.format("\n\t\t\treturn actualType->toWrapper<%s>();", cType));
                result.append("\n\t\t}");
            }

            return result.toString();
        }

    }

}
