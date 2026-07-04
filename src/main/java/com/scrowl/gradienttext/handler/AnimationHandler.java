package com.scrowl.gradienttext.handler;

import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientEngine;
import com.scrowl.gradienttext.gradient.GradientMode;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "gradienttext", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnimationHandler {

    private static long animationTime = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        animationTime++;

        updateItemAnimations(mc.player);
    }

    private static void updateItemAnimations(Player player) {
        updateItemGradient(player.getMainHandItem());
        updateItemGradient(player.getOffhandItem());

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                updateItemGradient(stack);
            }
        }
    }

    private static void updateItemGradient(ItemStack stack) {
        if (stack.isEmpty()) return;

        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        GradientData data = GradientData.fromItemStack(stack);
        if (data == null) return;

        if (data.getMode() == GradientMode.STATIC) return;

        String originalName = tag.getString("OriginalName");
        if (originalName.isEmpty()) {
            originalName = stack.getHoverName().getString();
            tag.putString("OriginalName", originalName);
        }

        float speed = data.getSpeed();
        Component gradientName;

        if (data.getMode() == GradientMode.SMOOTH) {
            gradientName = GradientEngine.applySmoothTransition(
                    originalName,
                    data.getColors(),
                    data.isBold(),
                    animationTime,
                    speed
            );
        } else if (data.getMode() == GradientMode.DYNAMIC) {
            gradientName = GradientEngine.applyAnimatedGradient(
                    originalName,
                    data.getColors(),
                    data.getDirection(),
                    data.isBold(),
                    animationTime,
                    speed
            );
        } else {
            return;
        }

        stack.setHoverName(gradientName);
    }
}
