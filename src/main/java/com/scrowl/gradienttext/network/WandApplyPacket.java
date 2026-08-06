package com.scrowl.gradienttext.network;

import com.scrowl.gradienttext.gradient.WandApply;
import com.scrowl.gradienttext.gradient.WandGradient;
import com.scrowl.gradienttext.item.GradientWandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WandApplyPacket {
    private final String gradientId;
    private final BlockPos center;
    private final int radius;
    private final String axis;

    public WandApplyPacket(String gradientId, BlockPos center, int radius, String axis) {
        this.gradientId = gradientId;
        this.center = center;
        this.radius = radius;
        this.axis = axis;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(gradientId);
        buf.writeBlockPos(center);
        buf.writeInt(radius);
        buf.writeUtf(axis);
    }

    public static WandApplyPacket decode(FriendlyByteBuf buf) {
        String gradientId = buf.readUtf();
        BlockPos center = buf.readBlockPos();
        int radius = buf.readInt();
        String axis = buf.readUtf();
        return new WandApplyPacket(gradientId, center, radius, axis);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() || !(held.getItem() instanceof GradientWandItem)) return;

            int r = Math.max(GradientWandItem.MIN_RADIUS, Math.min(GradientWandItem.MAX_RADIUS, radius));
            WandGradient gradient = WandGradient.get(gradientId);
            int changed = WandApply.applySurface(player.serverLevel(), center, r, axis, gradient);
            player.sendSystemMessage(Component.literal("Applied " + gradient.displayName
                    + " - " + changed + " blocks").withStyle(ChatFormatting.GREEN));
        });
        ctx.get().setPacketHandled(true);
    }
}
