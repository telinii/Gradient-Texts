package com.scrowl.gradienttext.render;

import com.scrowl.gradienttext.GradientTextMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundPatterns {

    public static class Pattern {
        public final String id;
        public final String displayName;
        public final List<String> blocks;
        public final boolean stars;
        public final int[] gradient;

        public Pattern(String id, String displayName, List<String> blocks) {
            this(id, displayName, blocks, false, null);
        }

        public Pattern(String id, String displayName, List<String> blocks, boolean stars) {
            this(id, displayName, blocks, stars, null);
        }

        public Pattern(String id, String displayName, List<String> blocks, boolean stars, int[] gradient) {
            this.id = id;
            this.displayName = displayName;
            this.blocks = blocks;
            this.stars = stars;
            this.gradient = gradient;
        }
    }

    public static final List<Pattern> PATTERNS = List.of(
            new Pattern("deepslate_vein", "Deepslate Vein", List.of(
                    "minecraft:deepslate", "minecraft:deepslate_bricks", "minecraft:cobbled_deepslate",
                    "minecraft:polished_deepslate", "minecraft:smooth_basalt", "minecraft:tuff",
                    "minecraft:andesite", "minecraft:polished_andesite", "minecraft:stone",
                    "minecraft:smooth_stone", "minecraft:calcite")),
            new Pattern("nether_temple", "Nether Temple", List.of(
                    "minecraft:netherite_block", "minecraft:blackstone", "minecraft:polished_blackstone",
                    "minecraft:polished_blackstone_bricks", "minecraft:chiseled_polished_blackstone",
                    "minecraft:nether_bricks", "minecraft:cracked_nether_bricks", "minecraft:red_nether_bricks",
                    "minecraft:netherrack", "minecraft:glowstone", "minecraft:gilded_blackstone")),
            new Pattern("end_vault", "End Vault", List.of(
                    "minecraft:crying_obsidian", "minecraft:obsidian", "minecraft:smooth_basalt",
                    "minecraft:purpur_pillar", "minecraft:purpur_block", "minecraft:amethyst_block",
                    "minecraft:budding_amethyst", "minecraft:end_stone_bricks", "minecraft:end_stone",
                    "minecraft:calcite")),
            new Pattern("desert_tomb", "Desert Tomb", List.of(
                    "minecraft:red_sandstone", "minecraft:chiseled_red_sandstone", "minecraft:cut_red_sandstone",
                    "minecraft:red_sand", "minecraft:red_terracotta", "minecraft:terracotta",
                    "minecraft:sand", "minecraft:sandstone", "minecraft:chiseled_sandstone",
                    "minecraft:cut_sandstone", "minecraft:calcite")),
            new Pattern("twisted_growth", "Twisted Growth", List.of(
                    "minecraft:sculk", "minecraft:warped_nylium", "minecraft:warped_stem",
                    "minecraft:warped_planks", "minecraft:dark_prismarine", "minecraft:prismarine",
                    "minecraft:prismarine_bricks", "minecraft:warped_wart_block", "minecraft:purpur_block",
                    "minecraft:sea_lantern")),
                        new Pattern("the_void", "The Void", List.of(), false,
                    new int[]{css("#000000"), css("#07070A"), css("#0E0E12"),
                            css("#16161B"), css("#1F1F26"), css("#292932")}),
            new Pattern("heaven", "Heaven", List.of(
                    "minecraft:light_blue_terracotta", "minecraft:white_terracotta", "minecraft:bone_block",
                    "minecraft:white_wool", "minecraft:white_concrete", "minecraft:quartz_block",
                    "minecraft:quartz_pillar", "minecraft:quartz_bricks", "minecraft:snow_block",
                    "minecraft:gold_block")),
            new Pattern("space", "Space", List.of(), true,
                    new int[]{css("#000000"), css("#000000")}),
            new Pattern("matrix", "Matrix", List.of(
                    "minecraft:black_concrete", "minecraft:green_concrete", "minecraft:green_terracotta",
                    "minecraft:green_wool", "minecraft:moss_block", "minecraft:lime_concrete",
                    "minecraft:lime_terracotta", "minecraft:lime_wool", "minecraft:emerald_block")),
            new Pattern("molten_core", "Molten Core", List.of(
                    "minecraft:blackstone", "minecraft:netherrack", "minecraft:red_concrete",
                    "minecraft:red_terracotta", "minecraft:orange_concrete", "minecraft:orange_terracotta",
                    "minecraft:yellow_concrete", "minecraft:gold_block", "minecraft:glowstone")),
            new Pattern("black_hole", "Black Hole", List.of())
    );

    private static final Map<String, TextureAtlasSprite> SPRITE_CACHE = new HashMap<>();

    public static List<Pattern> getAll() {
        return PATTERNS;
    }

    public static Pattern get(String id) {
        if (id != null) {
            for (Pattern p : PATTERNS) {
                if (p.id.equals(id)) return p;
            }
        }
        return PATTERNS.get(0);
    }

    public static TextureAtlasSprite sprite(String blockId) {
        TextureAtlasSprite s = SPRITE_CACHE.get(blockId);
        if (s != null) return s;
        try {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (block == null || block == Blocks.AIR) {
                GradientTextMod.LOGGER.warn("Pattern block not registered: {}", blockId);
                return null;
            }
            BlockState state = block.defaultBlockState();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            TextureAtlasSprite found = null;
            for (Direction dir : Direction.values()) {
                List<BakedQuad> quads = model.getQuads(state, dir, RandomSource.create());
                if (!quads.isEmpty() && quads.get(0).getSprite() != null) {
                    found = quads.get(0).getSprite();
                    break;
                }
            }
            if (found == null) {
                List<BakedQuad> quads = model.getQuads(state, null, RandomSource.create());
                if (!quads.isEmpty() && quads.get(0).getSprite() != null) found = quads.get(0).getSprite();
            }
            if (found == null) {
                GradientTextMod.LOGGER.warn("Pattern block texture missing: {}", blockId);
                return null;
            }
            SPRITE_CACHE.put(blockId, found);
            return found;
        } catch (Exception e) {
            GradientTextMod.LOGGER.warn("Pattern block texture missing: {} ({})", blockId, e.getMessage());
            return null;
        }
    }

    public static void render(GuiGraphics g, int width, int height, String patternId) {
        Pattern pattern = get(patternId);
        if (pattern.id.equals("black_hole")) {
            renderBlackHole(g, width, height);
            return;
        }
        // opaque base first so the GUI is never see-through
        g.fill(0, 0, width, height, 0xFF101016);

        int seed = pattern.id.hashCode();
        if (pattern.gradient != null && pattern.gradient.length >= 2) {
            renderGradient(g, width, height, pattern.gradient);
        } else {
            int bands = pattern.blocks.size();
            if (bands == 0) return;

            for (int y = 0; y < height; y += 16) {
                int baseBand = Math.min(bands - 1, (y * bands) / height);
                for (int x = 0; x < width; x += 16) {
                    // deterministic per-tile hash so the pattern is stable frame to frame
                    int h = hash(seed, x >> 4, y >> 4);
                    // wobble the band boundary per tile -> wavy, natural gradient
                    int band = clamp(baseBand + Math.floorMod(h, 5) - 2, 0, bands - 1);
                    // occasionally pull a neighbouring block for texture variety
                    int pick = clamp(band + (((h >>> 8) & 3) == 0 ? (((h >>> 16) & 1) * 2 - 1) : 0), 0, bands - 1);
                    TextureAtlasSprite sp = sprite(pattern.blocks.get(pick));
                    if (sp == null) continue;
                    g.blit(x, y, 0, 16, 16, sp);
                }
            }
        }

        // subtle dim + dark-top -> light-bottom fade
        g.fillGradient(0, 0, width, height, 0x20000000, 0x0A000000);

        if (pattern.stars) {
            renderStars(g, width, height, seed);
        }
    }

    private static void renderStars(GuiGraphics g, int width, int height, int seed) {
        for (int y = 0; y < height; y += 16) {
            // more stars at the top (zenith), fewer toward the horizon
            int dens = 4 + (int) (16 * (1f - (float) y / height));
            for (int x = 0; x < width; x += 16) {
                int h = hash(seed, (x >> 4) + 7, (y >> 4) * 7);
                if ((h & 63) >= dens) continue;
                int sx = x + ((h >>> 8) & 15);
                int sy = y + ((h >>> 16) & 15);
                int col = ((h >>> 20) & 1) == 0 ? 0xFFFFFFFF : 0xFFCCE8FF;
                g.fill(sx, sy, sx + 1, sy + 1, col);
                // a few larger, brighter stars
                if ((h >>> 24) < 8) {
                    g.fill(sx - 1, sy - 1, sx + 1, sy + 1, 0xFFFFFFFF);
                }
            }
        }
    }

    private static void renderGradient(GuiGraphics g, int width, int height, int[] stops) {
        int n = stops.length;
        for (int y = 0; y < height; y++) {
            float t = (float) y / height;
            float ft = t * (n - 1);
            int i = Math.min(n - 2, (int) ft);
            float f = ft - i;
            g.fill(0, y, width, y + 1, lerpColor(stops[i], stops[i + 1], f));
        }
    }

    // CSS-style hex color, e.g. css("#05050A")
    public static int css(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        return 0xFF000000 | (int) Long.parseLong(h, 16);
    }

    private static void renderBlackHole(GuiGraphics g, int width, int height) {
        // deep space backdrop
        renderGradient(g, width, height, new int[]{
                css("#020204"), css("#04040A"), css("#070713"), css("#0B0B1A")});
        renderStars(g, width, height, "black_hole".hashCode());

        int cx = width / 2;
        int cy = height / 2;
        int R = Math.min(width, height) / 3;

        // radial glow: nested ellipses from outer faint to inner bright
        int[] glowStops = {
                0xFF14162A, 0xFF1E2C4F, 0xFF413A78,
                0xFF7F59A6, 0xFFC86B42, 0xFFFFD9A0};
        int rings = 28;
        for (int i = rings; i >= 0; i--) {
            float t = (float) i / rings;            // 1 (outer) -> 0 (inner)
            float r = R * (0.98f - t * 0.55f);       // outer to ~0.43R
            int col = lerpColor(glowStops[0], glowStops[glowStops.length - 1], 1f - t);
            fillEllipse(g, cx, cy, r, r * 0.62f, col, height);
        }

        // accretion disk stretched along the horizontal
        fillEllipse(g, (int) (cx + R * 0.16), cy, R * 1.25f, R * 0.2f, 0xFFFFEAC0, height);

        // event horizon: solid black disk covers the center, leaving a bright rim
        fillEllipse(g, cx, cy, R * 0.42f, R * 0.42f, 0xFF000000, height);
    }

    private static void fillEllipse(GuiGraphics g, int cx, int cy, double rx, double ry, int color, int height) {
        int y0 = Math.max(0, (int) Math.floor(cy - ry));
        int y1 = Math.min(height, (int) Math.ceil(cy + ry));
        for (int y = y0; y < y1; y++) {
            double rel = (y - cy) / (double) ry;
            if (rel < -1 || rel > 1) continue;
            double dx = rx * Math.sqrt(1 - rel * rel);
            int x0 = (int) Math.floor(cx - dx);
            int x1 = (int) Math.ceil(cx + dx);
            g.fill(x0, y, x1, y + 1, color);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }

    private static int hash(int seed, int x, int y) {
        int h = seed;
        h = h * 31 + x;
        h = h * 31 + y;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h;
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }
}
