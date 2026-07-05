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
            "orange", "pink", "brown", "purple", "magenta", "cyan",
            "navy", "teal", "maroon", "olive", "lime", "sky",
            "indigo", "violet", "coral", "salmon", "crimson", "ruby",
            "amber", "jade", "mint", "lavender", "peach", "apricot",
            "beige", "ivory", "charcoal", "silver", "bronze", "copper",
            "gold_dark", "gold_light", "rose", "cherry", "wine",
            "forest", "emerald", "turquoise", "aqua_dark", "aqua_light",
            "neon_green", "neon_blue", "neon_pink", "neon_yellow",
            "pastel_red", "pastel_blue", "pastel_green", "pastel_yellow",
            "pastel_pink", "pastel_purple", "pastel_cyan", "pastel_orange"
    );

    public static final List<String> COMMON_HEX = Arrays.asList(
            "#FF0000", "#FF5500", "#FFAA00", "#FFFF00", "#55FF55",
            "#00FF00", "#00FFAA", "#00FFFF", "#00AAFF", "#0055FF",
            "#0000FF", "#5500FF", "#AA00FF", "#FF00FF", "#FF00AA",
            "#FF5555", "#55FF55", "#5555FF", "#FFFF55", "#55FFFF",
            "#FFFFFF", "#AAAAAA", "#555555", "#000000",
            "#FF1493", "#FF6347", "#FF7F50", "#FFA07A", "#FFD700",
            "#FFA500", "#FF8C00", "#FF4500", "#DC143C", "#B22222",
            "#CD853F", "#D2691E", "#8B4513", "#A0522D", "#F4A460",
            "#DEB887", "#D2B48C", "#BC8F8F", "#C0C0C0", "#808080",
            "#2F4F4F", "#008080", "#20B2AA", "#48D1CC", "#40E0D0",
            "#7FFFD4", "#66CDAA", "#8FBC8F", "#3CB371", "#2E8B57",
            "#228B22", "#006400", "#00FF7F", "#7CFC00", "#7FFF00",
            "#ADFF2F", "#9ACD32", "#90EE90", "#00FA9A", "#00FF00",
            "#32CD32", "#32CD32", "#00BFFF", "#1E90FF", "#6495ED",
            "#4169E1", "#0000CD", "#00008B", "#4B0082", "#8B008B",
            "#9932CC", "#BA55D3", "#DA70D6", "#EE82EE", "#DDA0DD",
            "#D8BFD8", "#E6E6FA", "#FFF0F5", "#FFE4E1", "#FFC0CB",
            "#FFB6C1", "#FF69B4", "#FF1493", "#C71585", "#DB7093",
            "#FFDAB9", "#FFE4B5", "#FFEBCD", "#FFE4C4", "#FFDEAD",
            "#F5DEB3", "#FAEBD7", "#FFF8DC", "#FAF0E6", "#FFF5EE",
            "#F0FFF0", "#F5FFFA", "#F0FFFF", "#F0F8FF", "#F8F8FF",
            "#FFFFF0", "#FFFFE0", "#FFFF00", "#FFFACD", "#FAFAD2",
            "#FFEFD5", "#FFFACD", "#EEE8AA", "#BDB76B", "#DAA520",
            "#FFB900", "#CD950C", "#8B8000", "#808000", "#556B2F",
            "#6B8E23", "#808000", "#008000", "#006400", "#004D00"
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
