package com.scrowl.gradienttext.network;

import com.scrowl.gradienttext.gradient.GradientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GradientRemovePacket {

    public void encode(FriendlyByteBuf buf) {
    }

    public static GradientRemovePacket decode(FriendlyByteBuf buf) {
        return new GradientRemovePacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (!heldItem.isEmpty()) {
                    GradientData.removeFromItemStack(heldItem);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
