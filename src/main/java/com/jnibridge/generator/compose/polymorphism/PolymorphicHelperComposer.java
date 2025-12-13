package com.jnibridge.generator.compose.polymorphism;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.generator.compose.TypeInfoComposer;
import com.jnibridge.generator.compose.jni.PtrWrapperJNIComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class PolymorphicHelperComposer implements Composer {

    public static final String POLYMORPHIC_CONVENIENCE_HEADER_FILENAME = "JniBridgePolyHelper.hpp";

    private static final String PLACEHOLDER_HANDLE_INCLUDE = "internalHandlePath";
    private static final String PLACEHOLDER_HELPER_FUNCTIONS = "helperFunctions";

    private final ClassInfo classInfo;

    @Override
    public String compose() {
        String template = ResourceUtils.load("com/jnibridge/internals/polymorphism/PolymorphicHandler.template");
        return TemplateUtils.substitute(template, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        final Map<String, String> replacements = new HashMap<>();

        replacements.put(PLACEHOLDER_HELPER_FUNCTIONS, getHelperFunctionReplacement());
        replacements.put(PLACEHOLDER_HANDLE_INCLUDE, String.format("../%s", PtrWrapperJNIComposer.INTERNAL_FILENAME));

        return replacements;
    }

    private String getHelperFunctionReplacement() {
        RawPolymorphicFuncComposer rawPolymorphicFuncComposer = new RawPolymorphicFuncComposer(classInfo);
        SharedPolymorphicFuncComposer sharedPolymorphicFuncComposer = new SharedPolymorphicFuncComposer(classInfo);

        StringBuilder result = new StringBuilder();

        result.append(rawPolymorphicFuncComposer.compose()).append("\n\n");
        result.append(sharedPolymorphicFuncComposer.compose()).append("\n\n");

        return result.toString();
    }

    public static String getHelperFilename(@NotNull final ClassInfo classInfo) {
        return String.format("%s_%s.helper.hpp", classInfo.getClazz().getPackage().getName().replace(".", "_"), classInfo.getClazz().getSimpleName());
    }

    @Getter
    public static abstract class PolymorphicFuncComposer implements Composer {

        private static final String PLACEHOLDER_HANDLE_TO_INSTANCE = "handleToInstance";

        private final ClassInfo polymorphicClass;
        private final String helperFunctionPrefix;

        private final String cType;
        private final String cTypeUnderscore;

        public PolymorphicFuncComposer(@NotNull final ClassInfo polymorphicClass, @NotNull final String helperFunctionPrefix) {
            this.polymorphicClass = polymorphicClass;
            this.helperFunctionPrefix = helperFunctionPrefix;
            this.cType = ClassInfoExtractor.extractClassCType(polymorphicClass.getClazz());
            this.cTypeUnderscore = cType.replace("::", "_");
        }

        @Override
        public @NotNull Map<String, String> getReplacements() {
            final Map<String, String> replacements = new HashMap<>();

            replacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE, cType);
            replacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE_UNDERSCORE, cTypeUnderscore);
            replacements.put(PLACEHOLDER_HANDLE_TO_INSTANCE, getHandleToInstanceReplacement());

            return replacements;
        }

        public abstract String getHandleToInstanceReplacement();

    }


}
