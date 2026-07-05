package com.scrowl.gradienttext.handler;

import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.WeakHashMap;

public class GradientEventHandler {

    private static final WeakHashMap<ItemStack, List<Component>> loreCache = new WeakHashMap<>();

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();

        if (stack.isEmpty() || !GradientData.hasGradient(stack)) {
            return;
        }

        GradientData data = GradientData.fromItemStack(stack);
        if (data == null) {
            return;
        }

        List<Component> tooltip = event.getToolTip();

        if (stack.hasTag()) {
            CompoundTag displayTag = stack.getTag().getCompound("display");
            if (displayTag.contains("Lore")) {
                ListTag loreTag = displayTag.getList("Lore", Tag.TAG_STRING);
                int loreIndex = 1;

                for (int i = 0; i < loreTag.size() && loreIndex < tooltip.size(); i++) {
                    try {
                        String loreStr = loreTag.getString(i);
                        Component loreLine = Component.Serializer.fromJson(loreStr);
                        if (loreLine != null) {
                            String loreText = loreLine.getString();
                            if (!loreText.isEmpty()) {
                                Component gradientLore = createGradientComponent(
                                        loreText, data.getColors(), data.getDirection(), data.isBold());
                                tooltip.set(loreIndex, gradientLore);
                            }
                            loreIndex++;
                        }
                    } catch (Exception e) {
                        GradientTextMod.LOGGER.warn("Failed to parse lore line: {}", e.getMessage());
                    }
                }
            }
        }
    }

    private Component createGradientComponent(String text, int[] colors, GradientDirection direction, boolean bold) {
        if (text.isEmpty()) {
            return Component.empty();
        }

        return GradientEngine.applyGradient(text, colors, direction, bold);
    }
}
