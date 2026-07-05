package com.scrowl.gradienttext.gradient;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class GradientEngine {

    public static Component applyGradient(String text, int[] colors, GradientDirection direction, boolean bold) {
        if (text == null || text.isEmpty() || colors == null || colors.length == 0) {
            return Component.literal(text);
        }

        MutableComponent result = Component.literal("");
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(Component.literal(String.valueOf(c)));
                continue;
            }

            float ratio = length > 1 ? (float) i / (float) (length - 1) : 0.0f;
            int color = interpolateColor(colors, ratio);
            result.append(Component.literal(String.valueOf(c)).withStyle(makeStyle(color, bold)));
        }

        return result;
    }

    public static Component applyAnimatedGradient(String text, int[] colors, GradientDirection direction, boolean bold, long timeMs, float speed) {
        if (text == null || text.isEmpty() || colors == null || colors.length == 0) {
            return Component.literal(text);
        }

        MutableComponent result = Component.literal("");
        int length = text.length();
        float timeOffset = (timeMs * speed * 0.01f) % 1.0f;

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(Component.literal(String.valueOf(c)));
                continue;
            }

            float ratio = length > 1 ? (float) i / (float) (length - 1) : 0.0f;
            float pos = (ratio + timeOffset) % 1.0f;
            int color = interpolateColor(colors, pos);
            result.append(Component.literal(String.valueOf(c)).withStyle(makeStyle(color, bold)));
        }

        return result;
    }

    public static Component applySmoothTransition(String text, int[] colors, boolean bold, long timeMs, float speed) {
        if (text == null || text.isEmpty() || colors == null || colors.length == 0) {
            return Component.literal(text);
        }

        float timeOffset = (timeMs * speed * 0.01f) % 1.0f;
        int color = interpolateColor(colors, timeOffset);
        Style sharedStyle = makeStyle(color, bold);

        MutableComponent result = Component.literal("");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            result.append(Component.literal(String.valueOf(c)).withStyle(sharedStyle));
        }

        return result;
    }

    private static Style makeStyle(int color, boolean bold) {
        TextColor textColor = TextColor.fromRgb(color);
        Style style = Style.EMPTY.withColor(textColor).withItalic(false);
        if (bold) {
            style = style.withBold(true);
        }
        return style;
    }

    private static int interpolateColor(int[] colors, float ratio) {
        if (colors.length == 1) return colors[0];

        float scaledRatio = ratio * colors.length;
        int index = (int) scaledRatio;
        float localRatio = scaledRatio - index;

        int c1 = colors[index % colors.length];
        int c2 = colors[(index + 1) % colors.length];

        return interpolateColors(c1, c2, localRatio);
    }

    private static int interpolateColors(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        float smooth = ratio * ratio * (3.0f - 2.0f * ratio);

        int r = (int) (r1 + (r2 - r1) * smooth);
        int g = (int) (g1 + (g2 - g1) * smooth);
        int b = (int) (b1 + (b2 - b1) * smooth);

        return (r << 16) | (g << 8) | b;
    }

    public static int parseColor(String str) {
        if (str == null || str.isEmpty()) return 0xFFFFFF;

        str = str.trim().toLowerCase();

        switch (str) {
            case "black": return 0x000000;
            case "dark_blue": case "darkblue": case "navy": return 0x0000AA;
            case "dark_green": case "darkgreen": case "forest": return 0x00AA00;
            case "dark_aqua": case "darkaqua": case "teal": return 0x00AAAA;
            case "dark_red": case "darkred": case "maroon": case "crimson": case "ruby": return 0xAA0000;
            case "dark_purple": case "darkpurple": case "indigo": return 0xAA00AA;
            case "gold": case "gold_dark": return 0xFFAA00;
            case "gold_light": return 0xFFD700;
            case "gray": case "grey": case "silver": return 0xAAAAAA;
            case "dark_gray": case "darkgrey": case "darkgray": case "charcoal": return 0x555555;
            case "blue": return 0x5555FF;
            case "green": case "lime": return 0x55FF55;
            case "aqua": case "cyan": case "sky": return 0x55FFFF;
            case "aqua_dark": return 0x00AAAA;
            case "aqua_light": return 0x7FFFD4;
            case "red": return 0xFF5555;
            case "light_purple": case "lightpurple": case "magenta": case "violet": return 0xFF55FF;
            case "yellow": return 0xFFFF55;
            case "white": case "ivory": case "beige": return 0xFFFFFF;
            case "orange": case "coral": case "peach": return 0xFF5500;
            case "pink": case "salmon": case "apricot": return 0xFFAACC;
            case "brown": case "bronze": case "copper": return 0x8B4513;
            case "purple": return 0x800080;
            case "olive": return 0x808000;
            case "cherry": case "wine": return 0xDC143C;
            case "emerald": return 0x50C878;
            case "jade": return 0x00A86B;
            case "mint": return 0x98FF98;
            case "lavender": return 0xE6E6FA;
            case "rose": return 0xFF007F;
            case "neon_green": return 0x39FF14;
            case "neon_blue": return 0x1F51FF;
            case "neon_pink": return 0xFF6EC7;
            case "neon_yellow": return 0xDFFF00;
            case "pastel_red": return 0xFFB3BA;
            case "pastel_blue": return 0xBAE1FF;
            case "pastel_green": return 0xBAFFC9;
            case "pastel_yellow": return 0xFFFFBA;
            case "pastel_pink": return 0xFFDFBA;
            case "pastel_purple": return 0xE8BAFF;
            case "pastel_cyan": return 0xBAFFFF;
            case "pastel_orange": return 0xFFD8BA;
            case "turquoise": return 0x40E0D0;
            default: break;
        }

        if (str.startsWith("#")) str = str.substring(1);

        if (str.length() == 6) {
            try {
                return Integer.parseInt(str, 16);
            } catch (NumberFormatException e) {
                return 0xFFFFFF;
            }
        }

        if (str.contains(",")) {
            String[] parts = str.split(",");
            if (parts.length == 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    return (r << 16) | (g << 8) | b;
                } catch (NumberFormatException e) {
                    return 0xFFFFFF;
                }
            }
        }

        return 0xFFFFFF;
    }
}
