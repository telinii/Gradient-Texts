package com.scrowl.gradienttext.config;

import com.scrowl.gradienttext.GradientTextMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;

public class ConfiguredIntegration {
    private static final String CONFIGURED_MOD_ID = "configured";

    public static void register() {
        if (!ModList.get().isLoaded(CONFIGURED_MOD_ID)) {
            return;
        }

        GradientTextMod.LOGGER.info("Configured mod detected, integration available via /gradient config command");
    }
}
