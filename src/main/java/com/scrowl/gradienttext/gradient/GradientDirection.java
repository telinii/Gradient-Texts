package com.scrowl.gradienttext.gradient;

import net.minecraft.nbt.StringTag;

public enum GradientDirection {
    HORIZONTAL("horizontal"),
    VERTICAL("vertical"),
    FIX("fix");

    private final String name;

    GradientDirection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public StringTag toTag() {
        return StringTag.valueOf(name);
    }

    public static GradientDirection fromString(String str) {
        for (GradientDirection dir : values()) {
            if (dir.name.equalsIgnoreCase(str)) {
                return dir;
            }
        }
        return HORIZONTAL;
    }
}
