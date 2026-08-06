package com.scrowl.gradienttext.gradient;

import com.scrowl.gradienttext.item.GradientWandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common-side application of gradients to the world. All methods run on the
 * server thread only.
 */
public class WandApply {

    /**
     * Surface wand: recolors only blocks with at least one exposed (non-occluding)
     * face inside the box.
     */
    public static int applySurface(ServerLevel level, BlockPos center, int radius, String axis, WandGradient gradient) {
        int r = Math.max(0, radius);
        if (r == 0) return 0;
        int count = 0;
        int span = r * 2 + 1;
        int seed = gradient.id.hashCode();
        boolean useBlocks = gradient.blocks != null && !gradient.blocks.isEmpty();
        for (int dy = -r; dy <= r; dy++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState old = level.getBlockState(p);
                    if (isSkipped(old)) continue;
                    if (!isExposed(level, p)) continue;

                    float t = axisT(p, center, r, span, axis);
                    BlockState ns = pick(gradient, useBlocks, t, seed, p);
                    if (ns == null || ns.equals(old) || ns.isAir() || ns.hasBlockEntity()) continue;
                    level.setBlock(p, ns, 3);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Depth wand: for each column in the box, starting from the surface block and
     * going down up to {@code depth} layers, recolors only whitelisted blocks.
     */
    public static int applyDepth(ServerLevel level, BlockPos center, int radius, int depth,
                                 WandGradient gradient, List<String> whitelist) {
        int r = Math.max(0, radius);
        if (r == 0) return 0;
        if (whitelist == null || whitelist.isEmpty()) return 0;
        Set<String> wl = new HashSet<>();
        for (String id : whitelist) wl.add(id.toLowerCase());

        int count = 0;
        int seed = gradient.id.hashCode();
        boolean useBlocks = gradient.blocks != null && !gradient.blocks.isEmpty();
        int span = r * 2 + 1;
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                int layer = 0;
                for (int dy = r; dy >= -r; dy--) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState old = level.getBlockState(p);
                    if (isSkipped(old)) continue;
                    layer++;
                    if (layer > depth) break;
                    if (!wl.contains(idOf(old.getBlock()))) continue;

                    float t = (float) (layer - 1) / Math.max(depth - 1, 1);
                    BlockState ns = pick(gradient, seed, t, p);
                    if (ns == null || ns.equals(old) || ns.isAir() || ns.hasBlockEntity()) continue;
                    level.setBlock(p, ns, 3);
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (!neighbor.canOcclude()) return true;
        }
        return false;
    }

    private static boolean isSkipped(BlockState state) {
        Block b = state.getBlock();
        return b == Blocks.AIR || b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR
                || b == Blocks.BEDROCK || b == Blocks.BARRIER
                || b == Blocks.WATER || b == Blocks.LAVA || state.hasBlockEntity();
    }

    private static BlockState pick(WandGradient gradient, int seed, float t, BlockPos p) {
        return pick(gradient, gradient.blocks != null && !gradient.blocks.isEmpty(), t, seed, p);
    }

    private static BlockState pick(WandGradient gradient, boolean useBlocks, float t, int seed, BlockPos p) {
        if (useBlocks) {
            int n = gradient.blocks.size();
            int base = Math.min(n - 1, (int) (t * (n - 1)));
            int h = hash(seed, p.getX(), p.getY(), p.getZ());
            int band = clamp(base + Math.floorMod(h, 5) - 2, 0, n - 1);
            int pull = clamp(band + (((h >>> 8) & 3) == 0 ? (((h >>> 16) & 1) * 2 - 1) : 0), 0, n - 1);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(gradient.blocks.get(pull)));
            return block == null || block == Blocks.AIR ? null : block.defaultBlockState();
        }
        int color = WandGradient.sample(gradient.colors, t);
        return WandPalette.nearest(color).defaultBlockState();
    }

    private static float axisT(BlockPos p, BlockPos center, int r, int span, String axis) {
        float t;
        if ("horizontal".equals(axis)) {
            t = (p.getX() - (center.getX() - r)) / (float) (span - 1);
        } else if ("depth".equals(axis)) {
            t = (p.getZ() - (center.getZ() - r)) / (float) (span - 1);
        } else {
            // vertical: top dark -> bottom light (matches the background patterns)
            t = ((center.getY() + r) - p.getY()) / (float) (span - 1);
        }
        return Math.max(0f, Math.min(1f, t));
    }

    private static int hash(int seed, int x, int y, int z) {
        int h = seed;
        h = h * 31 + x;
        h = h * 31 + y;
        h = h * 31 + z;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h;
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }

    private static String idOf(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key != null ? key.toString() : null;
    }
}