package com.jnibridge.generator.compose.jni.helper.polymorphism;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.generator.compose.TypeInfoComposer;
import com.jnibridge.generator.compose.jni.helper.JniBridgeHandleComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Composes JNI-specific helper-code to handle polymorphism on a C++/jni level.
 */
@RequiredArgsConstructor
public class PolymorphicHelperComposer implements Composer {

    public static final String POLYMORPHIC_CONVENIENCE_HEADER_FILENAME = "JniBridgePolymorphicHelper.hpp";

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
        replacements.put(PLACEHOLDER_HANDLE_INCLUDE, String.format("../%s", JniBridgeHandleComposer.INTERNAL_FILENAME));

        return replacements;
    }

    /**
     * Compose the replacements for the actual helper functions of the corresponding polymorphic type.
     *
     * <p>Contains helper for:</p>
     * <ul>
     *     <li>Raw types.</li>
     *     <li>std::shared_ptr</li>
     * </ul>
     *
     * @return A String representation of polymorphic helpers.
     */
    @NotNull
    private String getHelperFunctionReplacement() {
        RawPolymorphicFuncComposer rawPolymorphicFuncComposer = new RawPolymorphicFuncComposer(classInfo);
        SharedPolymorphicFuncComposer sharedPolymorphicFuncComposer = new SharedPolymorphicFuncComposer(classInfo);

        //noinspection StringBufferReplaceableByString
        StringBuilder result = new StringBuilder();

        result.append(rawPolymorphicFuncComposer.compose()).append("\n\n");
        result.append(sharedPolymorphicFuncComposer.compose()).append("\n\n");

        return result.toString();
    }

    /**
     * Function computes the jni-helper filename for the passed type.
     *
     * @param classInfo The class to create helper functions for.
     * @return A unique filename for the generated header file.
     */
    public static String getHelperFilename(@NotNull final ClassInfo classInfo) {
        return String.format("%s_%s.helper.hpp",
                classInfo.getClazz().getPackage().getName().replace(".", "_"),
                classInfo.getClazz().getSimpleName());
    }

    /**
     * Composes JNI-specific code to handle polymorphism on a C++/jni level.
     */
    @Getter
    public static abstract class PolymorphicFuncComposer implements Composer {

        private static final String PLACEHOLDER_HANDLE_TO_INSTANCE = "handleToInstance";

        private final ClassInfo polymorphicClass;
        private final String helperFunctionPrefix;

        private final String cType;
        private final String cTypeUnderscore;

        /**
         * Constructor.
         *
         * @param polymorphicClass     The type to generate the helper function for.
         * @param helperFunctionPrefix The function prefix for the generated helper function (to make the function-signature unique).
         */
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

        /**
         * Generate a String representation of the actual helper function.
         *
         * @return A String representation of the helper function.
         */
        public abstract String getHandleToInstanceReplacement();

    }


}
