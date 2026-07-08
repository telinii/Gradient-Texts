package com.scrowl.gradienttext.config;

import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

        if (forced != null) {
            GradientData data = forced.toGradientData();
            String customName = forced.hasCustomName() ? forced.getCustomName() : null;

            java.util.List<net.minecraft.network.chat.Component> tooltip = event.getToolTip();
            if (!tooltip.isEmpty()) {
                String nameText = customName != null ? customName : stack.getHoverName().getString();
                if (!nameText.isEmpty()) {
                    net.minecraft.network.chat.Component gradientName = data.applyGradient(nameText);
                    tooltip.set(0, gradientName);
                }
            }
        } else if (GradientConfig.get().isDefaultToolGradients() && isTool(stack)) {
            GradientData data = getDefaultToolGradient(stack);
            if (data != null) {
                java.util.List<net.minecraft.network.chat.Component> tooltip = event.getToolTip();
                if (!tooltip.isEmpty()) {
                    String nameText = stack.getHoverName().getString();
                    if (!nameText.isEmpty()) {
                        net.minecraft.network.chat.Component gradientName = data.applyGradient(nameText);
                        tooltip.set(0, gradientName);
                    }
                }
            }
        } else if (GradientConfig.get().isDefaultArmorGradients() && isArmor(stack)) {
            GradientData data = getDefaultArmorGradient(stack);
            if (data != null) {
                java.util.List<net.minecraft.network.chat.Component> tooltip = event.getToolTip();
                if (!tooltip.isEmpty()) {
                    String nameText = stack.getHoverName().getString();
                    if (!nameText.isEmpty()) {
                        net.minecraft.network.chat.Component gradientName = data.applyGradient(nameText);
                        tooltip.set(0, gradientName);
                    }
                }
            }
        }
    }

    public static void applyForcedGradients(ServerPlayer player) {
        if (player == null) return;
        if (GradientConfig.get().getForcedGradients().isEmpty() && GradientConfig.get().getBlacklistedItems().isEmpty() && !GradientConfig.get().isDefaultToolGradients() && !GradientConfig.get().isDefaultArmorGradients()) return;

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
            } else if (GradientConfig.get().isDefaultToolGradients() && isTool(stack)) {
                GradientData data = getDefaultToolGradient(stack);
                if (data != null) {
                    GradientData.setOnItemStack(stack, data);
                }
            } else if (GradientConfig.get().isDefaultArmorGradients() && isArmor(stack)) {
                GradientData data = getDefaultArmorGradient(stack);
                if (data != null) {
                    GradientData.setOnItemStack(stack, data);
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

    public static boolean isTool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem ||
               item instanceof PickaxeItem || item instanceof ShovelItem ||
               item instanceof HoeItem;
    }

    public static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public static int[] getArmorMaterialColors(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return null;
        ArmorMaterial material = armor.getMaterial();

        if (material == ArmorMaterials.LEATHER) return GradientConfig.LEATHER_ARMOR_COLORS;
        if (material == ArmorMaterials.CHAIN) return GradientConfig.CHAIN_ARMOR_COLORS;
        if (material == ArmorMaterials.IRON) return GradientConfig.IRON_ARMOR_COLORS;
        if (material == ArmorMaterials.GOLD) return GradientConfig.GOLD_ARMOR_COLORS;
        if (material == ArmorMaterials.DIAMOND) return GradientConfig.DIAMOND_ARMOR_COLORS;
        if (material == ArmorMaterials.NETHERITE) return GradientConfig.NETHERITE_ARMOR_COLORS;
        return null;
    }

    public static int[] getMaterialColors(ItemStack stack) {
        Item item = stack.getItem();
        Tier tier = null;

        if (item instanceof SwordItem s) tier = s.getTier();
        else if (item instanceof AxeItem a) tier = a.getTier();
        else if (item instanceof PickaxeItem p) tier = p.getTier();
        else if (item instanceof ShovelItem sh) tier = sh.getTier();
        else if (item instanceof HoeItem h) tier = h.getTier();

        if (tier == null) return null;

        if (tier == Tiers.WOOD) return GradientConfig.WOOD_COLORS;
        if (tier == Tiers.STONE) return GradientConfig.STONE_COLORS;
        if (tier == Tiers.IRON) return GradientConfig.IRON_COLORS;
        if (tier == Tiers.GOLD) return GradientConfig.GOLD_COLORS;
        if (tier == Tiers.DIAMOND) return GradientConfig.DIAMOND_COLORS;
        if (tier == Tiers.NETHERITE) return GradientConfig.NETHERITE_COLORS;
        return null;
    }

    public static GradientData getDefaultToolGradient(ItemStack stack) {
        if (!GradientConfig.get().isDefaultToolGradients()) return null;
        if (!isTool(stack)) return null;
        if (GradientConfig.get().isItemBlacklisted(getItemId(stack))) return null;

        int[] colors = getMaterialColors(stack);
        if (colors == null) return null;

        GradientMode mode = GradientMode.fromString(GradientConfig.get().getDefaultGradientMode());
        return new GradientData(colors, GradientDirection.HORIZONTAL, mode, false, 1.0f);
    }

    public static GradientData getDefaultArmorGradient(ItemStack stack) {
        if (!GradientConfig.get().isDefaultArmorGradients()) return null;
        if (!isArmor(stack)) return null;
        if (GradientConfig.get().isItemBlacklisted(getItemId(stack))) return null;

        int[] colors = getArmorMaterialColors(stack);
        if (colors == null) return null;

        GradientMode mode = GradientMode.fromString(GradientConfig.get().getDefaultGradientMode());
        return new GradientData(colors, GradientDirection.HORIZONTAL, mode, false, 1.0f);
    }
}
