package com.jnibridge.generator.compose.jni.helper.polymorphism;

import com.jnibridge.generator.compose.Placeholder;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.SortedSet;

/**
 * Compose Polymorphic helper functions for Raw-types.
 */
public class RawPolymorphicFuncComposer extends PolymorphicHelperComposer.PolymorphicFuncComposer {

    private static final String FUNC_NAME_PREFIX = "baseHandle_to_";

    /**
     * Constructor.
     *
     * @param polymorphicClass The class to generate the helper function for.
     */
    public RawPolymorphicFuncComposer(@NotNull final ClassInfo polymorphicClass) {
        super(polymorphicClass, FUNC_NAME_PREFIX);
    }

    @Override
    public String compose() {
        String template = ResourceUtils.load("com/jnibridge/internals/polymorphism/PolymorphicHandlerFunc.raw.template");
        return TemplateUtils.substitute(template, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        Map<String, String> replacements = super.getReplacements();
        replacements.put(Placeholder.INSTANCE_TO_JAVA_FULL_PATH, getInstanceToJFullPath());
        return replacements;
    }

    @Override
    public String getHandleToInstanceReplacement() {
        final StringBuilder result = new StringBuilder();

        final SortedSet<Class<?>> subclasses = getPolymorphicClass().getSubclasses();
        boolean firstIteration = true;
        boolean shouldDynamicCast = subclasses.size() > 1;

        for (Class<?> subclass : subclasses) {
            final String subclassCType = ClassInfoExtractor.extractClassCType(subclass);

            if (shouldDynamicCast) {
                result.append(firstIteration ? "\t\tif " : "\n\t\telse if ");
                firstIteration = false;

                result.append(String.format("(auto* actualType = dynamic_cast<jnibridge::internal::Handle<%s>*>(handle)) {\n\t\t\t", subclassCType));
                result.append(String.format("return actualType->getAs<%s>(env);", getCType()));
                result.append("\n\t\t}");
            } else {
                result.append(String.format("\t\tauto* actualType = static_cast<jnibridge::internal::Handle<%s>*>(handle);\n\t\t", subclassCType));
                result.append("return actualType->get();");
            }
        }
        if (shouldDynamicCast) { result.append("\n\t\treturn nullptr;"); }

        return result.toString();
    }

    /**
     * @return Replacement for {@link com.jnibridge.generator.compose.Placeholder#INSTANCE_TO_JAVA_FULL_PATH}.
     */
    private String getInstanceToJFullPath() {
        final StringBuilder result = new StringBuilder();

        final SortedSet<Class<?>> subclasses = getPolymorphicClass().getSubclasses();
        boolean firstIteration = true;
        boolean shouldDynamicCast = subclasses.size() > 1;

        for (Class<?> subclass : subclasses) {
            final String subclassCType = ClassInfoExtractor.extractClassCType(subclass);

            if(shouldDynamicCast) {
                result.append(firstIteration ? "\t\tif " : "\n\t\telse if ");
                firstIteration = false;

                result.append(String.format("(auto* actualType = dynamic_cast<%s*>(instance)) {", subclassCType));
                result.append(String.format("\n\t\t\treturn \"%s\";", subclass.getName().replace(".", "/")));
                result.append("\n\t\t}");
            } else {
                result.append(String.format("\t\tauto* actualType = static_cast<%s*>(instance);", subclassCType));
                result.append(String.format("\n\t\treturn \"%s\";", subclass.getName().replace(".", "/")));
            }
        }

        if (shouldDynamicCast) { result.append("\n\t\treturn nullptr;"); }
        return result.toString();
    }
}
