package com.scrowl.gradienttext.network;

import com.scrowl.gradienttext.config.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenConfigScreenPacket {

    public void encode(FriendlyByteBuf buf) {
    }

    public static OpenConfigScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenConfigScreenPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new ConfigScreen(mc.screen));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
