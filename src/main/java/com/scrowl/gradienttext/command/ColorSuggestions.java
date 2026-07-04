package com.scrowl.gradienttext.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ColorSuggestions {
    public static final List<String> NAMED_COLORS = Arrays.asList(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
            "dark_purple", "gold", "gray", "dark_gray", "blue", "green",
            "aqua", "red", "light_purple", "yellow", "white",
            "orange", "pink", "brown", "purple", "magenta", "cyan"
    );

    public static final List<String> COMMON_HEX = Arrays.asList(
            "#FF0000", "#FF5500", "#FFAA00", "#FFFF00", "#55FF55",
            "#00FF00", "#00FFAA", "#00FFFF", "#00AAFF", "#0055FF",
            "#0000FF", "#5500FF", "#AA00FF", "#FF00FF", "#FF00AA",
            "#FF5555", "#55FF55", "#5555FF", "#FFFF55", "#55FFFF",
            "#FFFFFF", "#AAAAAA", "#555555", "#000000"
    );

    public static final List<String> DIRECTION = Arrays.asList("horizontal", "vertical", "fix");
    public static final List<String> MODE = Arrays.asList("static", "dynamic", "smooth");
    public static final List<String> BOOLEAN = Arrays.asList("true", "false", "yes", "no", "on", "off");

    public static final SuggestionProvider<CommandSourceStack> COLORS = (ctx, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();

        for (String color : NAMED_COLORS) {
            if (color.startsWith(remaining)) {
                builder.suggest(color);
            }
        }

        for (String hex : COMMON_HEX) {
            if (hex.toLowerCase().startsWith(remaining)) {
                builder.suggest(hex);
            }
        }

        builder.suggest("#RRGGBB");
        builder.suggest("R,G,B");

        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSourceStack> DIRECTIONS = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(DIRECTION, builder);
    };

    public static final SuggestionProvider<CommandSourceStack> MODES = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(MODE, builder);
    };

    public static final SuggestionProvider<CommandSourceStack> BOOLEANS = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(BOOLEAN, builder);
    };
}
