package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.annotations.modifiers.Specialized;
import com.jnibridge.generator.model.TypeInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Composes string representations of {@link TypeInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class TypeInfoComposer implements Composer {

    @NonNull
    private final TypeInfo typeInfo;

    @Override
    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        final boolean isConst = typeInfo.hasAnnotation(Const.class);
        final String cTypeReplacement = String.format("%s%s", isConst ? "const " : "", typeInfo.getCType());

        Optional<Specialized> custom = typeInfo.getAnnotation(Specialized.class);

        replacements.put(Placeholder.C_TYPE, Composer.getReplacement(cTypeReplacement, custom.map(Specialized::cType).orElse(null)));
        replacements.put(Placeholder.C_TYPE_UNDERSCORE, cTypeReplacement.replace("::", "_").replace("<", "_").replace(">", ""));

        replacements.put(Placeholder.JNI_TYPE, Composer.getReplacement(typeInfo.getJniType(), custom.map(Specialized::jniType).orElse(null)));

        String id = Optional.ofNullable(typeInfo.getId()).orElse("");
        replacements.put(Placeholder.ID, id);

        replacements.put(Placeholder.C_VAR, Composer.getReplacement(Placeholder.C_VAR + id, custom.map(Specialized::cVar).orElse(null)));
        replacements.put(Placeholder.JNI_VAR, Composer.getReplacement(Placeholder.JNI_VAR + id, custom.map(Specialized::jniVar).orElse(null)));

        replacements.put(Placeholder.JAVA_PATH, typeInfo.getType().getName().replace(".", "/"));

        replacements.put(Placeholder.JNI_CLEANUP, typeInfo.getCleanupLogic());

        addTemplateReplacements(replacements);
        return replacements;
    }

    private void addTemplateReplacements(@NotNull final Map<String, String> replacements) {
        // replace template argument types
        LinkedList<String> cTemplateArgumentTypes = getTypeInfo().getCTemplateArgumentTypes();
        LinkedList<Class<?>> javaTemplateArgumentTypes = getTypeInfo().getJavaTemplateArgumentTypes();

        if (cTemplateArgumentTypes != null && javaTemplateArgumentTypes != null) {
            for (int i = 0; i < cTemplateArgumentTypes.size(); ++i) {

                replacements.put(String.format("%s_%d", Placeholder.C_TEMPLATE_TYPE, i), cTemplateArgumentTypes.get(i));
                replacements.put(String.format("%s_%d", Placeholder.C_TEMPLATE_TYPE_UNDERSCORE, i), cTemplateArgumentTypes.get(i).replace("::", "_"));

                final String jTemplateArgumentReplacement = Optional.ofNullable(javaTemplateArgumentTypes.get(i))
                        .map(Class::getName)
                        .map(name -> name.replace(".", "/"))
                        .orElse("$INVALID MAPPING");
                replacements.put(String.format("%s_%d", Placeholder.JAVA_TEMPLATE_PATH, i), jTemplateArgumentReplacement);
            }
        }
    }
}
