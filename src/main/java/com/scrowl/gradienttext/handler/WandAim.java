package com.scrowl.gradienttext.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side helper: computes the block the player's crosshair is pointing at,
 * traced up to a long reach so the wands can be used without walking up to a
 * block. Falls back to the block at the end of the ray when nothing is hit.
 */
public final class WandAim {
    public static final double REACH = 32.0;

    private WandAim() {
    }

    public static BlockPos getAimPos(Player player, Level level) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.x * REACH, look.y * REACH, look.z * REACH);
        HitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) hit).getBlockPos();
        }
        return BlockPos.containing(end);
    }
}