package com.scrowl.gradienttext.handler;

import com.scrowl.gradienttext.item.DepthGradientWandItem;
import com.scrowl.gradienttext.item.GradientWandItem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Renders a wireframe box over the region the held wand would affect, based on
 * the block the player is currently aiming at. Purely client side.
 */
@Mod.EventBusSubscriber(modid = "gradienttext", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WandPreviewHandler {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) return;

        BlockPos pos = WandAim.getAimPos(mc.player, mc.level);

        int x0, y0, z0, x1, y1, z1;
        float r, g, b;
        if (held.getItem() instanceof GradientWandItem) {
            int radius = GradientWandItem.getRadius(held);
            x0 = pos.getX() - radius; y0 = pos.getY() - radius; z0 = pos.getZ() - radius;
            x1 = pos.getX() + radius + 1; y1 = pos.getY() + radius + 1; z1 = pos.getZ() + radius + 1;
            r = 0.3f; g = 1.0f; b = 1.0f;
        } else if (held.getItem() instanceof DepthGradientWandItem) {
            int radius = DepthGradientWandItem.getRadius(held);
            int depth = DepthGradientWandItem.getDepth(held);
            x0 = pos.getX() - radius; y0 = pos.getY() - depth + 1; z0 = pos.getZ() - radius;
            x1 = pos.getX() + radius + 1; y1 = pos.getY() + 1; z1 = pos.getZ() + radius + 1;
            r = 1.0f; g = 0.7f; b = 0.2f;
        } else {
            return;
        }

        // The render-level pose stack is not camera-translated, so translate by
        // -camera to draw in absolute world coordinates (like the vanilla highlight).
        var poseStack = event.getPoseStack();
        var cam = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
        VertexConsumer wire = source.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, wire, x0, y0, z0, x1, y1, z1, r, g, b, 1.0f);

        poseStack.popPose();
    }
}