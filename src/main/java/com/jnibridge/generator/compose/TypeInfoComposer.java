package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.model.TypeInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Composes string representations of {@link TypeInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class TypeInfoComposer implements Composer{

    // TypeInfo - related Placeholders...
    public static final String PLACEHOLDER_C_TYPE = "cType";
    public static final String PLACEHOLDER_C_VAR = "cVar";

    public static final String PLACEHOLDER_JNI_TYPE = "jniType";
    public static final String PLACEHOLDER_JNI_VAR = "jniVar";
    public static final String PLACEHOLDER_ID = "id";

    @NonNull
    private final TypeInfo typeInfo;

    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        final boolean isConst = typeInfo.hasAnnotation(Const.class);
        final String cTypeReplacement = String.format("%s%s", isConst ? "const " : "", typeInfo.getCType());

        replacements.put(PLACEHOLDER_C_TYPE, cTypeReplacement);
        replacements.put(PLACEHOLDER_JNI_TYPE, typeInfo.getJniType());

        String id = Optional.ofNullable(typeInfo.getId()).orElse("");
        replacements.put(PLACEHOLDER_ID, id);

        replacements.put(PLACEHOLDER_C_VAR, PLACEHOLDER_C_VAR + id);
        replacements.put(PLACEHOLDER_JNI_VAR, PLACEHOLDER_JNI_VAR + id);

        return replacements;
    }

}
