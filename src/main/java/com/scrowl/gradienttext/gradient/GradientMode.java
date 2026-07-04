package com.scrowl.gradienttext.gradient;

import net.minecraft.util.StringRepresentable;

public enum GradientMode implements StringRepresentable {
    STATIC("static", "Static"),
    DYNAMIC("dynamic", "Dynamic"),
    SMOOTH("smooth", "Smooth (Lethality)");

    private final String name;
    private final String displayName;

    GradientMode(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static GradientMode fromString(String str) {
        for (GradientMode mode : values()) {
            if (mode.name.equalsIgnoreCase(str) || mode.displayName.equalsIgnoreCase(str)) {
                return mode;
            }
        }
        return STATIC;
    }
}
