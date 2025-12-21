package com.jnibridge.generator.helper.polymorphism;

import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

public class SharedPolymorphicFuncComposer extends PolymorphicHelperComposer.PolymorphicFuncComposer {

    private static final String FUNC_NAME_PREFIX = "baseHandle_to_shared_";

    public SharedPolymorphicFuncComposer(@NotNull final ClassInfo polymorphicClass) {
        super(polymorphicClass, FUNC_NAME_PREFIX);
    }

    @Override
    public String compose() {
        String template = ResourceUtils.load("com/jnibridge/internals/polymorphism/PolymorphicHandlerFunc.shared.template");
        return TemplateUtils.substitute(template, getReplacements());
    }


    @Override
    public String getHandleToInstanceReplacement() {
        final StringBuilder result = new StringBuilder();

        final SortedSet<ClassInfo> subclasses = getPolymorphicClass().getSubclasses();
        boolean firstIteration = true;

        for(ClassInfo subclass : subclasses) {
            final String subclassCType = ClassInfoExtractor.extractClassCType(subclass.getClazz());

            result.append(firstIteration ? "\t\tif " : "\n\t\telse if ");
            firstIteration = false;

            result.append(String.format("(auto* actualType = dynamic_cast<jnibridge::internal::Handle<%s>*>(handle)) {", subclassCType));
            result.append(String.format("\n\t\t\treturn actualType->getAsShared<%s>();", getCType()));
            result.append("\n\t\t}");
        }
        result.append("\n\n\t\treturn nullptr;");

        return result.toString();
    }
}
