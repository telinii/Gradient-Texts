package com.scrowl.gradienttext.gradient;

import java.util.List;

/**
 * Common-side registry of building gradients for the gradient wand.
 * MUST NOT reference any net.minecraft.client.* classes: it is also read
 * by the server when applying gradients to the world.
 *
 * Each gradient carries both a color ramp (for previews / fallback) and a
 * ramp of specific block ids (dark -> light) used when detailing a build.
 */
public class WandGradient {
    public final String id;
    public final String displayName;
    public final int[] colors;
    public final List<String> blocks;

    public WandGradient(String id, String displayName, int[] colors, List<String> blocks) {
        this.id = id;
        this.displayName = displayName;
        this.colors = colors;
        this.blocks = blocks;
    }

    private static final List<WandGradient> ALL = List.of(
            new WandGradient("deepslate_vein", "Deepslate Vein",
                    new int[]{0x2B2F36, 0x3A3F47, 0x4A5058, 0x5B6168, 0x6C727A, 0x7D848C, 0x8E959D},
                    List.of("minecraft:deepslate", "minecraft:deepslate_bricks", "minecraft:cobbled_deepslate",
                            "minecraft:polished_deepslate", "minecraft:smooth_basalt", "minecraft:tuff",
                            "minecraft:andesite", "minecraft:polished_andesite", "minecraft:stone",
                            "minecraft:smooth_stone", "minecraft:calcite")),
            new WandGradient("nether_temple", "Nether Temple",
                    new int[]{0x2B2521, 0x3E2E26, 0x5A3A28, 0x7A4630, 0x9E5A38, 0xC27540, 0xE8A050},
                    List.of("minecraft:netherite_block", "minecraft:blackstone", "minecraft:polished_blackstone",
                            "minecraft:polished_blackstone_bricks", "minecraft:chiseled_polished_blackstone",
                            "minecraft:nether_bricks", "minecraft:cracked_nether_bricks", "minecraft:red_nether_bricks",
                            "minecraft:netherrack", "minecraft:glowstone", "minecraft:gilded_blackstone")),
            new WandGradient("end_vault", "End Vault",
                    new int[]{0x2E2436, 0x3D2C4A, 0x4E3660, 0x624277, 0x78508F, 0x8F60A8, 0xA87FC0},
                    List.of("minecraft:crying_obsidian", "minecraft:obsidian", "minecraft:smooth_basalt",
                            "minecraft:purpur_pillar", "minecraft:purpur_block", "minecraft:amethyst_block",
                            "minecraft:budding_amethyst", "minecraft:end_stone_bricks", "minecraft:end_stone",
                            "minecraft:calcite")),
            new WandGradient("desert_tomb", "Desert Tomb",
                    new int[]{0x6E3A2A, 0x8A4B32, 0xA85E3C, 0xC2744A, 0xDE8B5A, 0xE8A878, 0xF2C9A0},
                    List.of("minecraft:red_sandstone", "minecraft:chiseled_red_sandstone", "minecraft:cut_red_sandstone",
                            "minecraft:red_sand", "minecraft:red_terracotta", "minecraft:terracotta",
                            "minecraft:sand", "minecraft:sandstone", "minecraft:chiseled_sandstone",
                            "minecraft:cut_sandstone", "minecraft:calcite")),
            new WandGradient("twisted_growth", "Twisted Growth",
                    new int[]{0x1E2B2B, 0x2A3F3E, 0x375655, 0x476F6D, 0x598A87, 0x6FA6A2, 0x89C3BE},
                    List.of("minecraft:sculk", "minecraft:warped_nylium", "minecraft:warped_stem",
                            "minecraft:warped_planks", "minecraft:dark_prismarine", "minecraft:prismarine",
                            "minecraft:prismarine_bricks", "minecraft:warped_wart_block", "minecraft:purpur_block",
                            "minecraft:sea_lantern")),
            new WandGradient("the_void", "The Void",
                    new int[]{0x000000, 0x07070A, 0x0E0E12, 0x16161B, 0x1F1F26, 0x292932},
                    List.of("minecraft:black_concrete", "minecraft:blackstone", "minecraft:polished_blackstone",
                            "minecraft:deepslate", "minecraft:deepslate_bricks", "minecraft:cobbled_deepslate",
                            "minecraft:polished_deepslate", "minecraft:smooth_basalt")),
            new WandGradient("heaven", "Heaven",
                    new int[]{0x9FC4D8, 0xB8D0E0, 0xD8E6EE, 0xE8EFE8, 0xF0F4F0, 0xF8FAF8, 0xFFF8E0},
                    List.of("minecraft:light_blue_terracotta", "minecraft:white_terracotta", "minecraft:bone_block",
                            "minecraft:white_wool", "minecraft:white_concrete", "minecraft:quartz_block",
                            "minecraft:quartz_pillar", "minecraft:quartz_bricks", "minecraft:snow_block",
                            "minecraft:gold_block")),
            new WandGradient("space", "Space",
                    new int[]{0x000000, 0x0A0A12, 0x14141F},
                    List.of("minecraft:black_concrete", "minecraft:blue_concrete", "minecraft:dark_prismarine",
                            "minecraft:crying_obsidian", "minecraft:purple_concrete", "minecraft:magenta_concrete",
                            "minecraft:end_stone")),
            new WandGradient("matrix", "Matrix",
                    new int[]{0x0A140A, 0x142C14, 0x1F4520, 0x2E5E2E, 0x3F7A3F, 0x5CA15C, 0x7FC87F},
                    List.of("minecraft:black_concrete", "minecraft:green_concrete", "minecraft:green_terracotta",
                            "minecraft:green_wool", "minecraft:moss_block", "minecraft:lime_concrete",
                            "minecraft:lime_terracotta", "minecraft:lime_wool", "minecraft:emerald_block")),
            new WandGradient("molten_core", "Molten Core",
                    new int[]{0x24150E, 0x3E1E10, 0x5C2A12, 0x7E3A16, 0xA54E1C, 0xD06A24, 0xF8A032},
                    List.of("minecraft:blackstone", "minecraft:netherrack", "minecraft:red_concrete",
                            "minecraft:red_terracotta", "minecraft:orange_concrete", "minecraft:orange_terracotta",
                            "minecraft:yellow_concrete", "minecraft:gold_block", "minecraft:glowstone"))
    );

    public static List<WandGradient> getAll() {
        return ALL;
    }

    public static WandGradient get(String id) {
        if (id != null) {
            for (WandGradient g : ALL) {
                if (g.id.equals(id)) return g;
            }
        }
        return ALL.get(0);
    }

    /**
     * Sample a color (0xRRGGBB) from the ramp at normalized position t (0..1).
     */
    public static int sample(int[] colors, float t) {
        if (colors == null || colors.length == 0) return 0xFFFFFF;
        if (colors.length == 1) return colors[0];
        t = Math.max(0f, Math.min(1f, t));
        float f = t * (colors.length - 1);
        int i = Math.min(colors.length - 2, (int) f);
        float l = f - i;
        int c1 = colors[i];
        int c2 = colors[i + 1];
        int r = (int) (((c1 >> 16) & 0xFF) + ((((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * l));
        int g = (int) (((c1 >> 8) & 0xFF) + ((((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * l));
        int b = (int) ((c1 & 0xFF) + (((c2 & 0xFF) - (c1 & 0xFF)) * l));
        return (r << 16) | (g << 8) | b;
    }
}
