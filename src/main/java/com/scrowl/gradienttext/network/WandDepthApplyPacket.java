package com.scrowl.gradienttext.network;

import com.scrowl.gradienttext.gradient.WandApply;
import com.scrowl.gradienttext.gradient.WandGradient;
import com.scrowl.gradienttext.item.DepthGradientWandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WandDepthApplyPacket {
    private final String gradientId;
    private final BlockPos center;
    private final int radius;
    private final int depth;
    private final List<String> whitelist;

    public WandDepthApplyPacket(String gradientId, BlockPos center, int radius, int depth, List<String> whitelist) {
        this.gradientId = gradientId;
        this.center = center;
        this.radius = radius;
        this.depth = depth;
        this.whitelist = whitelist;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(gradientId);
        buf.writeBlockPos(center);
        buf.writeInt(radius);
        buf.writeInt(depth);
        buf.writeInt(whitelist.size());
        for (String id : whitelist) buf.writeUtf(id);
    }

    public static WandDepthApplyPacket decode(FriendlyByteBuf buf) {
        String gradientId = buf.readUtf();
        BlockPos center = buf.readBlockPos();
        int radius = buf.readInt();
        int depth = buf.readInt();
        int size = buf.readInt();
        List<String> whitelist = new ArrayList<>();
        for (int i = 0; i < Math.min(size, 512); i++) {
            whitelist.add(buf.readUtf());
        }
        return new WandDepthApplyPacket(gradientId, center, radius, depth, whitelist);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() || !(held.getItem() instanceof DepthGradientWandItem)) return;

            if (whitelist.isEmpty()) return;
            int r = Math.max(DepthGradientWandItem.MIN_RADIUS,
                    Math.min(DepthGradientWandItem.MAX_RADIUS, radius));
            int d = Math.max(DepthGradientWandItem.MIN_DEPTH,
                    Math.min(DepthGradientWandItem.MAX_DEPTH, depth));
            WandGradient gradient = WandGradient.get(gradientId);
            int changed = WandApply.applyDepth(player.serverLevel(), center, r, d, gradient, whitelist);
            player.sendSystemMessage(Component.literal("Depth painted " + gradient.displayName
                    + " - " + changed + " blocks").withStyle(ChatFormatting.GREEN));
        });
        ctx.get().setPacketHandled(true);
    }
}