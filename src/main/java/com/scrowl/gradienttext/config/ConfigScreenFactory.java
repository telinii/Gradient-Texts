package com.scrowl.gradienttext.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;

public class ConfigScreenFactory {
    private static final String CONFIGURED_MOD_ID = "configured";

    public static boolean isConfiguredLoaded() {
        return ModList.get().isLoaded(CONFIGURED_MOD_ID);
    }

    public static Screen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
}
