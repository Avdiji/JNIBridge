package com.jnibridge.examples.mappings.oop;

import com.jnibridge.annotations.BridgeClass;

@BridgeClass(isEnum = true, namespace = "jnibridge::examples")
public enum Color {

    Red(0),
    Green(1),
    Blue(2);

    private int colorCode;
    Color(final int colorCode) { this.colorCode = colorCode;}

    public static Color fromInt(final int colorCode) {
        for (Color color : values()) {
            if (color.colorCode == colorCode) {
                return color;
            }
        }
        throw new IllegalArgumentException("Unknown color code: " + colorCode);
    }


    public int toInt() { return colorCode; }

}
