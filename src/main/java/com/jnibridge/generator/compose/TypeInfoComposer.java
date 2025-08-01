package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.generator.model.TypeInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for composing string representations of a {@link TypeInfo}.
 *
 * <p>
 * Implementations of this class are responsible for transforming
 * the metadata contained in a {@link TypeInfo} instance into a
 * specific output format, such as JNI declarations, JSON metadata, ...
 * </p>
 */
@Getter
@RequiredArgsConstructor
public abstract class TypeInfoComposer implements Composer{

    // TypeInfo - related Placeholders...
    public static final String PLACEHOLDER_C_TYPE = "cType";
    public static final String PLACEHOLDER_C_VAR = "cVar";

    public static final String PLACEHOLDER_JNI_TYPE = "jniType";
    public static final String PLACEHOLDER_JNI_VAR = "jniVar";

    @NonNull
    private final TypeInfo typeInfo;

    /**
     * Returns a map of placeholder-to-value pairs used to replace
     * the placeholders defined in the type-mapping templates.
     *
     * @return a map of template placeholders to their resolved replacement values
     */
    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        final boolean isConst = typeInfo.hasAnnotation(Const.class);
        final String cTypeReplacement = String.format("%s%s", isConst ? "const " : "", typeInfo.getCType());

        replacements.put(PLACEHOLDER_C_TYPE, cTypeReplacement);
        replacements.put(PLACEHOLDER_JNI_TYPE, typeInfo.getJniType());

        replacements.put(PLACEHOLDER_C_VAR, PLACEHOLDER_C_VAR);
        replacements.put(PLACEHOLDER_JNI_VAR, PLACEHOLDER_JNI_VAR + Optional.ofNullable(typeInfo.getId()).orElse(""));

        return replacements;
    }

}
