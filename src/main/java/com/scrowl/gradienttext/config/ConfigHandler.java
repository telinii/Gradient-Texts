package com.scrowl.gradienttext.config;

import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.gradient.GradientData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ConfigHandler {
    private static long lastCheck = 0;
    private static final long CHECK_INTERVAL = 20;

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyForcedGradients(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        long now = player.level().getGameTime();
        if (now - lastCheck < CHECK_INTERVAL) return;
        lastCheck = now;

        applyForcedGradients(player);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        String itemId = getItemId(stack);
        if (itemId == null) return;

        if (GradientConfig.get().isItemBlacklisted(itemId)) return;
        if (GradientData.hasGradient(stack)) return;

        GradientConfig.ItemGradientEntry forced = GradientConfig.get().getForcedGradient(itemId);
        if (forced != null && forced.hasCustomName()) {
            event.getToolTip().add(Component.literal("  Name: " + forced.getCustomName()).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    public static void applyForcedGradients(ServerPlayer player) {
        if (player == null) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String itemId = getItemId(stack);
            if (itemId == null) continue;

            if (GradientConfig.get().isItemBlacklisted(itemId)) {
                if (GradientData.hasGradient(stack)) {
                    GradientData.removeFromItemStack(stack);
                }
                continue;
            }

            if (GradientData.hasGradient(stack)) continue;

            GradientConfig.ItemGradientEntry forced = GradientConfig.get().getForcedGradient(itemId);
            if (forced != null) {
                GradientData data = forced.toGradientData();
                GradientData.setOnItemStack(stack, data);

                if (forced.hasCustomName()) {
                    Component gradientName = data.applyGradient(forced.getCustomName());
                    stack.setHoverName(gradientName);
                }
            }
        }
    }

    public static void forceApplyAll(ServerPlayer player) {
        if (player == null) return;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            String itemId = getItemId(stack);
            if (itemId == null) continue;

            GradientData.removeFromItemStack(stack);

            GradientConfig.ItemGradientEntry forced = GradientConfig.get().getForcedGradient(itemId);
            if (forced != null && !GradientConfig.get().isItemBlacklisted(itemId)) {
                GradientData data = forced.toGradientData();
                GradientData.setOnItemStack(stack, data);
                if (forced.hasCustomName()) {
                    Component gradientName = data.applyGradient(forced.getCustomName());
                    stack.setHoverName(gradientName);
                }
            }
        }
    }

    public static boolean canApplyGradient(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemId = getItemId(stack);
        if (itemId == null) return false;
        return !GradientConfig.get().isItemBlacklisted(itemId);
    }

    public static String getItemId(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null ? key.toString() : null;
    }
}
