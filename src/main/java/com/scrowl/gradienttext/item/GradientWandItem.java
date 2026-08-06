package com.scrowl.gradienttext.item;

import com.scrowl.gradienttext.gradient.WandGradient;
import com.scrowl.gradienttext.gui.WandPickerScreen;
import com.scrowl.gradienttext.handler.WandAim;
import com.scrowl.gradienttext.network.NetworkHandler;
import com.scrowl.gradienttext.network.WandApplyPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

import java.util.List;

public class GradientWandItem extends Item {
    public static final String TAG_GRADIENT = "WandGradient";
    public static final String TAG_RADIUS = "WandRadius";
    public static final String TAG_AXIS = "WandAxis";

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 9;
    public static final int DEFAULT_RADIUS = 3;

    public GradientWandItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static void setGradientId(ItemStack stack, String id) {
        stack.getOrCreateTag().putString(TAG_GRADIENT, id != null ? id : "");
    }

    public static String getGradientId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(TAG_GRADIENT) : "";
    }

    public static int getRadius(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_RADIUS)) return DEFAULT_RADIUS;
        return stack.getTag().getInt(TAG_RADIUS);
    }

    public static void setRadius(ItemStack stack, int radius) {
        stack.getOrCreateTag().putInt(TAG_RADIUS, Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius)));
    }

    public static String getAxis(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_AXIS)) return "vertical";
        return stack.getTag().getString(TAG_AXIS);
    }

    public static void setAxis(ItemStack stack, String axis) {
        stack.getOrCreateTag().putString(TAG_AXIS, "horizontal".equals(axis) ? "horizontal" : "vertical");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (player.isCrouching()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.setScreen(new WandPickerScreen());
                    }
                });
            } else {
                String gid = getGradientId(stack);
                if (gid.isEmpty()) gid = WandGradient.getAll().get(0).id;
                NetworkHandler.CHANNEL.sendToServer(new WandApplyPacket(
                        gid, WandAim.getAimPos(player, level), getRadius(stack), getAxis(stack)));
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
                        mc.setScreen(new WandPickerScreen());
                    }
                });
                return InteractionResult.CONSUME;
            }
            ItemStack stack = player.getItemInHand(ctx.getHand());
            String gid = getGradientId(stack);
            if (gid.isEmpty()) gid = WandGradient.getAll().get(0).id;
            NetworkHandler.CHANNEL.sendToServer(new WandApplyPacket(
                    gid, ctx.getClickedPos(), getRadius(stack), getAxis(stack)));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String gid = getGradientId(stack);
        WandGradient g = gid.isEmpty() ? null : WandGradient.get(gid);
        tooltip.add(Component.literal("Gradient: ").append(g != null ? g.displayName : "none").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Radius: " + getRadius(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Axis: " + getAxis(stack)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
