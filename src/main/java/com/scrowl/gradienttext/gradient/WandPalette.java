package com.scrowl.gradienttext.gradient;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Common-side color -> block palette used by the gradient wand.
 * Matches a gradient color to the nearest colored building block.
 */
public class WandPalette {
    private static final class Entry {
        final Block block;
        final int r, g, b;

        Entry(Block block, int hex) {
            this.block = block;
            this.r = (hex >> 16) & 0xFF;
            this.g = (hex >> 8) & 0xFF;
            this.b = hex & 0xFF;
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        int[] cols = {
                0xF9FFFE, 0x9D9D97, 0x474F52, 0x1D1D21, 0x835432, 0xB02E26, 0xF9801D, 0xFED83D,
                0x80C71F, 0x5E7C16, 0x169C9C, 0x3AB3DA, 0x3C44AA, 0x8932B8, 0xC74EBD, 0xF38BAA};
        Block[] concrete = {
                Blocks.WHITE_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.BLACK_CONCRETE,
                Blocks.BROWN_CONCRETE, Blocks.RED_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.YELLOW_CONCRETE,
                Blocks.LIME_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
                Blocks.BLUE_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.PINK_CONCRETE};
        Block[] terracotta = {
                Blocks.WHITE_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.BLACK_TERRACOTTA,
                Blocks.BROWN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
                Blocks.LIME_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA,
                Blocks.BLUE_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.PINK_TERRACOTTA};
        Block[] wool = {
                Blocks.WHITE_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.GRAY_WOOL, Blocks.BLACK_WOOL,
                Blocks.BROWN_WOOL, Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL,
                Blocks.LIME_WOOL, Blocks.GREEN_WOOL, Blocks.CYAN_WOOL, Blocks.LIGHT_BLUE_WOOL,
                Blocks.BLUE_WOOL, Blocks.PURPLE_WOOL, Blocks.MAGENTA_WOOL, Blocks.PINK_WOOL};

        for (int i = 0; i < 16; i++) {
            ENTRIES.add(new Entry(concrete[i], cols[i]));
            ENTRIES.add(new Entry(terracotta[i], cols[i]));
            ENTRIES.add(new Entry(wool[i], cols[i]));
        }
    }

    public static Block nearest(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        Block best = Blocks.WHITE_CONCRETE;
        int bestDist = Integer.MAX_VALUE;
        for (Entry e : ENTRIES) {
            int dr = r - e.r;
            int dg = g - e.g;
            int db = b - e.b;
            int dist = dr * dr * 3 + dg * dg * 6 + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                best = e.block;
            }
        }
        return best;
    }
}
