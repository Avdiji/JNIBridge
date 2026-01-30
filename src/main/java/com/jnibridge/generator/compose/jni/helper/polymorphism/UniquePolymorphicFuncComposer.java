package com.jnibridge.generator.compose.jni.helper.polymorphism;

import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

/**
 * Compose Polymorphic helper functions for types, wrapped in <code>std::shared_ptr</code>.
 */
public class UniquePolymorphicFuncComposer extends PolymorphicHelperComposer.PolymorphicFuncComposer {

    private static final String FUNC_NAME_PREFIX = "baseHandle_to_unique_";

    /**
     * Constructor.
     *
     * @param polymorphicClass The class to generate the helper function for.
     */
    public UniquePolymorphicFuncComposer(@NotNull final ClassInfo polymorphicClass) {
        super(polymorphicClass, FUNC_NAME_PREFIX);
    }

    @Override
    public String compose() {
        String template = ResourceUtils.load("com/jnibridge/internals/polymorphism/PolymorphicHandlerFunc.unique.template");
        return TemplateUtils.substitute(template, getReplacements());
    }


    @Override
    public String getHandleToInstanceReplacement() {
        final StringBuilder result = new StringBuilder();

        final SortedSet<Class<?>> subclasses = getPolymorphicClass().getSubclasses();
        boolean firstIteration = true;
        boolean shouldDynamicCast = subclasses.size() > 1;

        for (Class<?> subclass : subclasses) {
            final String subclassCType = ClassInfoExtractor.extractClassCType(subclass);

            if(shouldDynamicCast) {
                result.append(firstIteration ? "\t\tif " : "\n\t\telse if ");
                firstIteration = false;

                result.append(String.format("(auto* actualType = dynamic_cast<jnibridge::internal::Handle<%s>*>(handle)) {\n\t\t\t", subclassCType));
                result.append(String.format("return actualType->getAsUnique<%s>(env);", getCType()));
                result.append("\n\t\t}");
            } else {
                result.append(String.format("\t\tauto* actualType = static_cast<jnibridge::internal::Handle<%s>*>(handle);\n\t\t", subclassCType));
                result.append(String.format("return actualType->getAsUnique<%s>(env);", getCType()));
            }
        }
        if (shouldDynamicCast) { result.append("\n\t\treturn nullptr;"); }
        return result.toString();
    }
}
