package com.scrowl.gradienttext.handler;

import com.scrowl.gradienttext.config.GradientConfig;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientEngine;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "gradienttext", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GradientGlowHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColors itemColors = event.getItemColors();

        // Register for ALL items - the handler will check if gradient exists
        List<net.minecraft.world.item.Item> allItems = new ArrayList<>();
        net.minecraft.core.registries.BuiltInRegistries.ITEM.forEach(item -> {
            if (item != Items.AIR) {
                allItems.add(item);
            }
        });

        ItemColor gradientColorHandler = (stack, tintIndex) -> {
            // tintIndex 1 is the enchantment glint layer
            if (tintIndex != 1) return -1;
            if (!GradientConfig.get().isGradientGlowEnabled()) return -1;
            if (!stack.isEnchanted()) return -1;
            if (!GradientData.hasGradient(stack)) return -1;

            GradientData data = GradientData.fromItemStack(stack);
            if (data == null) return -1;

            int[] colors = data.getColors();
            if (colors == null || colors.length == 0) return -1;

            long time = com.scrowl.gradienttext.handler.AnimationHandler.getAnimationTime();
            float speed = data.getSpeed();
            float timeOffset = (time * speed * 0.01f) % 1.0f;

            int color = GradientEngine.getInterpolatedColor(colors, timeOffset);
            return color;
        };

        // Register for each item
        for (net.minecraft.world.item.Item item : allItems) {
            itemColors.register(gradientColorHandler, item);
        }
    }
}
