package com.scrowl.gradienttext.network;

import com.scrowl.gradienttext.GradientTextMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GradientTextMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextId(),
                GradientApplyPacket.class,
                GradientApplyPacket::encode,
                GradientApplyPacket::decode,
                GradientApplyPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(),
                GradientRemovePacket.class,
                GradientRemovePacket::encode,
                GradientRemovePacket::decode,
                GradientRemovePacket::handle
        );

        CHANNEL.registerMessage(
                nextId(),
                OpenConfigScreenPacket.class,
                OpenConfigScreenPacket::encode,
                OpenConfigScreenPacket::decode,
                OpenConfigScreenPacket::handle
        );
    }
}
