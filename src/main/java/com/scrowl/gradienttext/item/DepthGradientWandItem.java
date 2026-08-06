package com.scrowl.gradienttext.item;

import com.scrowl.gradienttext.gradient.WandGradient;
import com.scrowl.gradienttext.gui.DepthWandPickerScreen;
import com.scrowl.gradienttext.handler.WandAim;
import com.scrowl.gradienttext.network.NetworkHandler;
import com.scrowl.gradienttext.network.WandDepthApplyPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth wand: recolors a region down a column, up to a range, only touching
 * whitelisted blocks. Kept as a separate item from the surface wand.
 */
public class DepthGradientWandItem extends Item {
    public static final String TAG_GRADIENT = "WandGradient";
    public static final String TAG_RADIUS = "WandRadius";
    public static final String TAG_DEPTH = "WandDepth";
    public static final String TAG_WHITELIST = "WandWhitelist";

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 9;
    public static final int DEFAULT_RADIUS = 3;

    public static final int MIN_DEPTH = 1;
    public static final int MAX_DEPTH = 32;
    public static final int DEFAULT_DEPTH = 6;

    public static final List<String> DEFAULT_WHITELIST = List.of(
            "minecraft:stone", "minecraft:cobblestone", "minecraft:deepslate",
            "minecraft:cobbled_deepslate", "minecraft:andesite", "minecraft:diorite",
            "minecraft:granite", "minecraft:basalt");

    public DepthGradientWandItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static void setGradientId(ItemStack stack, String id) {
        stack.getOrCreateTag().putString(TAG_GRADIENT, id != null ? id : "");
    }

    public static String getGradientId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(TAG_GRADIENT) : "";
    }

    public static int getDepth(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_DEPTH)) return DEFAULT_DEPTH;
        return stack.getTag().getInt(TAG_DEPTH);
    }

    public static void setDepth(ItemStack stack, int depth) {
        stack.getOrCreateTag().putInt(TAG_DEPTH, Math.max(MIN_DEPTH, Math.min(MAX_DEPTH, depth)));
    }

    public static int getRadius(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_RADIUS)) return DEFAULT_RADIUS;
        return stack.getTag().getInt(TAG_RADIUS);
    }

    public static void setRadius(ItemStack stack, int radius) {
        stack.getOrCreateTag().putInt(TAG_RADIUS, Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius)));
    }

    public static List<String> getWhitelist(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_WHITELIST)) {
            return new ArrayList<>(DEFAULT_WHITELIST);
        }
        ListTag tag = stack.getTag().getList(TAG_WHITELIST, net.minecraft.nbt.Tag.TAG_STRING);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            out.add(tag.getString(i));
        }
        return out;
    }

    public static void setWhitelist(ItemStack stack, List<String> whitelist) {
        ListTag tag = new ListTag();
        for (String id : whitelist) {
            tag.add(StringTag.valueOf(id));
        }
        stack.getOrCreateTag().put(TAG_WHITELIST, tag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (player.isCrouching()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.setScreen(new DepthWandPickerScreen());
                    }
                });
            } else {
                String gid = getGradientId(stack);
                if (gid.isEmpty()) gid = WandGradient.getAll().get(0).id;
                NetworkHandler.CHANNEL.sendToServer(new WandDepthApplyPacket(
                        gid, WandAim.getAimPos(player, level), getRadius(stack), getDepth(stack), getWhitelist(stack)));
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) {
            if (player.isCrouching()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.setScreen(new DepthWandPickerScreen());
                    }
                });
                return InteractionResult.CONSUME;
            }
            ItemStack stack = player.getItemInHand(ctx.getHand());
            String gid = getGradientId(stack);
            if (gid.isEmpty()) gid = WandGradient.getAll().get(0).id;
            NetworkHandler.CHANNEL.sendToServer(new WandDepthApplyPacket(
                    gid, ctx.getClickedPos(), getRadius(stack), getDepth(stack), getWhitelist(stack)));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String gid = getGradientId(stack);
        WandGradient g = gid.isEmpty() ? null : WandGradient.get(gid);
        tooltip.add(Component.literal("Gradient: ").append(g != null ? g.displayName : "none").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Depth: " + getDepth(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Whitelist: " + getWhitelist(stack).size() + " blocks").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}