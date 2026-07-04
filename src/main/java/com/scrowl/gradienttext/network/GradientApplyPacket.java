package com.scrowl.gradienttext.network;

import com.scrowl.gradienttext.config.ConfigHandler;
import com.scrowl.gradienttext.config.GradientConfig;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GradientApplyPacket {
    private final int[] colors;
    private final GradientDirection direction;
    private final GradientMode mode;
    private final boolean bold;
    private final float speed;

    public GradientApplyPacket(int[] colors, GradientDirection direction, GradientMode mode, boolean bold, float speed) {
        this.colors = colors;
        this.direction = direction;
        this.mode = mode;
        this.bold = bold;
        this.speed = speed;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(colors.length);
        for (int color : colors) {
            buf.writeInt(color);
        }
        buf.writeUtf(direction.getName());
        buf.writeUtf(mode.getName());
        buf.writeBoolean(bold);
        buf.writeFloat(speed);
    }

    public static GradientApplyPacket decode(FriendlyByteBuf buf) {
        int length = buf.readInt();
        int[] colors = new int[length];
        for (int i = 0; i < length; i++) {
            colors[i] = buf.readInt();
        }
        GradientDirection direction = GradientDirection.fromString(buf.readUtf());
        GradientMode mode = GradientMode.fromString(buf.readUtf());
        boolean bold = buf.readBoolean();
        float speed = buf.readFloat();
        return new GradientApplyPacket(colors, direction, mode, bold, speed);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (!heldItem.isEmpty()) {
                    String itemId = ConfigHandler.getItemId(heldItem);
                    if (itemId != null && GradientConfig.get().isItemBlacklisted(itemId)) {
                        return;
                    }
                    GradientData data = new GradientData(colors, direction, mode, bold, speed);
                    GradientData.setOnItemStack(heldItem, data);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
