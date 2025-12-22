package com.jnibridge.generator.compose.jni.helper;

import com.jnibridge.generator.compose.Composer;
import com.jnibridge.mapper.GlobalMapperRegistry;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class JniBridgeExceptionComposer implements Composer {
    private static final String PLACEHOLDER_EXCEPTION_FUNC = "exceptionFunc";
    private static final String PLACEHOLDER_HANDLE_INCLUDE = "internalHandlePath";
    public static final String INTERNAL_FILENAME = "JniBridgeExceptionHandler.hpp";


    @Override
    public String compose() {
        final String exceptionHandler = ResourceUtils.load("com/jnibridge/internals/exception/JniBridgeExceptionHandler.hpp");
        return TemplateUtils.substitute(exceptionHandler, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        final Map<String, String> replacements = new HashMap<>();
        replacements.put(PLACEHOLDER_EXCEPTION_FUNC, new JniBridgeExceptionFuncComposer().compose());
        replacements.put(PLACEHOLDER_HANDLE_INCLUDE, String.format("%s", JniBridgeHandleComposer.INTERNAL_FILENAME));
        return replacements;
    }


    private static class JniBridgeExceptionFuncComposer implements Composer {
        private static final String PLACEHOLDER_EXCEPTION_FUNC_LOGIC = "exceptionFuncLogic";

        @Override
        public String compose() {
            final String exceptionHandlerFunc = ResourceUtils.load("com/jnibridge/internals/exception/MappedExceptionHandlerFunc.template");
            return TemplateUtils.substitute(exceptionHandlerFunc, getReplacements());
        }

        @Override
        public @NotNull Map<String, String> getReplacements() {
            final Map<String, String> replacements = new HashMap<>();
            replacements.put(PLACEHOLDER_EXCEPTION_FUNC_LOGIC, getFuncBodyReplacement());
            return replacements;
        }

        private String getFuncBodyReplacement() {
            final StringBuilder result = new StringBuilder();

            boolean isFirstIteration = true;
            final Map<String, Class<? extends Throwable>> exceptionRegistry = GlobalMapperRegistry.exceptionRegistry;
            for (final Map.Entry<String, Class<? extends Throwable>> exceptionMappingEntry : exceptionRegistry.entrySet()) {

                String keyword = isFirstIteration ? "if" : "else if";
                isFirstIteration = false;

                // condition
                result.append("\t\t")
                        .append(keyword)
                        .append(" (dynamic_cast<const ")
                        .append(exceptionMappingEntry.getKey())
                        .append("*>(&e)) {\n");

                // condition body
                result.append("\t\t\treturn env->FindClass(\"")
                        .append(exceptionMappingEntry.getValue().getCanonicalName().replace(".", "/"))
                        .append("\");\n\t\t}\n");
            }

            return result.toString();
        }

    }
}
