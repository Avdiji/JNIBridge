package com.jnibridge.utils;

import org.apache.commons.text.StringSubstitutor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for performing simple string template substitutions using named placeholders.
 */
public class TemplateUtils {

    // Private constructor to prevent instantiation
    private TemplateUtils() { }

    /**
     * Method for performing simple template substitution using named placeholders.
     * <p>
     * Template variables use the syntax <code>$varName</code> and are replaced with values
     * provided via varargs in key-value pairs.
     * <p>
     * Example:
     * <pre>{@code
     * String template = "int $cVar = static_cast<int>($jniVar);";
     * String result = TemplateUtils.substitute(template, "cVar", "value1", "jniVar", "value2");
     * }</pre>
     * <p>
     * Produces:
     * <pre>{@code
     * int value1 = static_cast<int>(value2);
     * }</pre>
     *
     * @param template The templated string.
     * @param args The substitution key-value pairs.
     *
     * @return The substituted template.
     */
    public static String substitute(String template, String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments must be in key-value pairs.");
        }

        Map<String, Object> variables = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            variables.put(args[i], args[i + 1]);
        }

        return new StringSubstitutor(variables, "$", null).replace(template);
    }

}
