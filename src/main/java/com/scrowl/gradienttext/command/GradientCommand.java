package com.scrowl.gradienttext.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.scrowl.gradienttext.config.ConfigManager;
import com.scrowl.gradienttext.config.ConfigScreen;
import com.scrowl.gradienttext.config.GradientConfig;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientEngine;
import com.scrowl.gradienttext.gradient.GradientMode;
import com.scrowl.gradienttext.network.NetworkHandler;
import com.scrowl.gradienttext.network.OpenConfigScreenPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class GradientCommand {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        var speedArg = Commands.argument("speed", FloatArgumentType.floatArg(GradientData.MIN_SPEED, GradientData.MAX_SPEED))
                .executes(ctx -> applyGradientFull(ctx));

        var boldArg = Commands.argument("bold", StringArgumentType.word())
                .suggests(ColorSuggestions.BOOLEANS)
                .then(speedArg)
                .executes(ctx -> applyGradientFull(ctx));

        var modeArg = Commands.argument("mode", StringArgumentType.word())
                .suggests(ColorSuggestions.MODES)
                .then(boldArg)
                .executes(ctx -> applyGradientFull(ctx));

        var dirArg = Commands.argument("direction", StringArgumentType.word())
                .suggests(ColorSuggestions.DIRECTIONS)
                .then(modeArg)
                .executes(ctx -> applyGradientFull(ctx));

        var optionsNode = Commands.literal("options")
                .then(dirArg);

        var colorsArg = Commands.argument("colors", StringArgumentType.word())
                .suggests(ColorSuggestions.COLORS)
                .then(optionsNode)
                .executes(ctx -> applyGradient(ctx));

        dispatcher.register(Commands.literal("gradient")
                .then(colorsArg)
                .then(Commands.literal("remove")
                        .executes(GradientCommand::removeGradient))
                .then(Commands.literal("info")
                        .executes(GradientCommand::showInfo))
                .then(Commands.literal("config")
                        .executes(GradientCommand::openConfig))
                .then(Commands.literal("blacklist")
                        .then(Commands.argument("itemId", StringArgumentType.word())
                                .executes(GradientCommand::blacklistItem)))
                .then(Commands.literal("unblacklist")
                        .then(Commands.argument("itemId", StringArgumentType.word())
                                .executes(GradientCommand::unblacklistItem)))
                .then(Commands.literal("reload")
                        .executes(GradientCommand::reloadConfig))
                .then(Commands.literal("help")
                        .executes(GradientCommand::showHelp))
        );
    }

    private static int[] parseColorsArgument(String colorsStr) {
        if (colorsStr == null || colorsStr.isEmpty()) return null;

        String[] parts = colorsStr.split("-");
        if (parts.length < 1) return null;

        int[] colors = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            colors[i] = GradientEngine.parseColor(parts[i].trim());
        }
        return colors;
    }

    private static int applyGradient(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack heldItem = player.getMainHandItem();

            if (heldItem.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Hold an item to apply gradient!").withStyle(ChatFormatting.RED));
                return 0;
            }

            String colorsStr = StringArgumentType.getString(ctx, "colors");
            int[] colors = parseColorsArgument(colorsStr);
            if (colors == null || colors.length < 2) {
                ctx.getSource().sendFailure(Component.literal("Need 2+ colors separated by dashes!").withStyle(ChatFormatting.RED));
                return 0;
            }

            GradientData data = new GradientData(colors, GradientDirection.HORIZONTAL, GradientMode.STATIC, false, 1.0f);
            GradientData.setOnItemStack(heldItem, data);

            StringBuilder applied = new StringBuilder();
            for (int i = 0; i < colors.length; i++) {
                if (i > 0) applied.append(" -> ");
                applied.append(String.format("#%06X", colors[i]));
            }

            ctx.getSource().sendSuccess(() -> Component.literal("Gradient applied! " + applied).withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int applyGradientFull(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack heldItem = player.getMainHandItem();

            if (heldItem.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Hold an item to apply gradient!").withStyle(ChatFormatting.RED));
                return 0;
            }

            String colorsStr = StringArgumentType.getString(ctx, "colors");
            int[] colors = parseColorsArgument(colorsStr);
            if (colors == null || colors.length < 2) {
                ctx.getSource().sendFailure(Component.literal("Need 2+ colors separated by dashes!").withStyle(ChatFormatting.RED));
                return 0;
            }

            GradientDirection dir = GradientDirection.HORIZONTAL;
            GradientMode mode = GradientMode.STATIC;
            boolean bold = false;
            float speed = 1.0f;

            try { dir = GradientDirection.fromString(StringArgumentType.getString(ctx, "direction")); } catch (Exception ignored) {}
            try { mode = GradientMode.fromString(StringArgumentType.getString(ctx, "mode")); } catch (Exception ignored) {}
            try {
                String boldStr = StringArgumentType.getString(ctx, "bold").toLowerCase();
                bold = boldStr.equals("true") || boldStr.equals("yes") || boldStr.equals("on");
            } catch (Exception ignored) {}
            try { speed = FloatArgumentType.getFloat(ctx, "speed"); } catch (Exception ignored) {}

            GradientData data = new GradientData(colors, dir, mode, bold, speed);
            GradientData.setOnItemStack(heldItem, data);

            StringBuilder applied = new StringBuilder();
            for (int i = 0; i < colors.length; i++) {
                if (i > 0) applied.append(" -> ");
                applied.append(String.format("#%06X", colors[i]));
            }

            final GradientDirection fDir = dir;
            final GradientMode fMode = mode;
            final boolean fBold = bold;
            final float fSpeed = speed;

            ctx.getSource().sendSuccess(() -> Component.literal("Gradient applied! " + applied + " [" + fDir.getName() + "/" + fMode.getName() + "/" + (fBold ? "bold" : "normal") + "/" + fSpeed + "x]").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int openConfig(CommandContext<CommandSourceStack> ctx) {
        try {
            ctx.getSource().sendSuccess(() -> Component.literal("Opening config GUI...").withStyle(ChatFormatting.GREEN), false);
            Minecraft.getInstance().tell(() -> {
                Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen));
            });
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int blacklistItem(CommandContext<CommandSourceStack> ctx) {
        try {
            String itemId = StringArgumentType.getString(ctx, "itemId");
            GradientConfig.get().addBlacklistedItem(itemId);
            ConfigManager.save();
            ctx.getSource().sendSuccess(() -> Component.literal("Blacklisted: " + itemId).withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int unblacklistItem(CommandContext<CommandSourceStack> ctx) {
        try {
            String itemId = StringArgumentType.getString(ctx, "itemId");
            GradientConfig.get().removeBlacklistedItem(itemId);
            ConfigManager.save();
            ctx.getSource().sendSuccess(() -> Component.literal("Removed: " + itemId).withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int removeGradient(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Hold an item!").withStyle(ChatFormatting.RED));
                return 0;
            }
            if (!GradientData.hasGradient(heldItem)) {
                ctx.getSource().sendFailure(Component.literal("No gradient on this item!").withStyle(ChatFormatting.RED));
                return 0;
            }
            GradientData.removeFromItemStack(heldItem);
            ctx.getSource().sendSuccess(() -> Component.literal("Gradient removed!").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Hold an item!").withStyle(ChatFormatting.RED));
                return 0;
            }
            GradientData data = GradientData.fromItemStack(heldItem);
            if (data == null) {
                ctx.getSource().sendSuccess(() -> Component.literal("No gradient.").withStyle(ChatFormatting.YELLOW), false);
                return 1;
            }
            StringBuilder colorStr = new StringBuilder();
            for (int i = 0; i < data.getColors().length; i++) {
                if (i > 0) colorStr.append(", ");
                colorStr.append(String.format("#%06X", data.getColors()[i]));
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Colors: " + colorStr).withStyle(ChatFormatting.WHITE), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Dir: " + data.getDirection().getName() + " | Mode: " + data.getMode().getName() + " | Bold: " + data.isBold() + " | Speed: " + data.getSpeed()).withStyle(ChatFormatting.WHITE), false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int showHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("Gradient Commands:").withStyle(ChatFormatting.AQUA), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient <colors>").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient <colors> options <dir> <mode> <bold> <speed>").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("").withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Examples:").withStyle(ChatFormatting.YELLOW), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient red-blue").withStyle(ChatFormatting.YELLOW), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient red-yellow-green-blue").withStyle(ChatFormatting.YELLOW), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient red-blue options vertical dynamic true 2.0").withStyle(ChatFormatting.YELLOW), false);
        ctx.getSource().sendSuccess(() -> Component.literal("").withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient remove - Remove gradient").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient info - Show info").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient config - Open config GUI").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient reload - Reload config from disk").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  /gradient help - Show help").withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.literal("").withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Colors: red-blue-#FF5500 (dash-separated, 2+ colors)").withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Options: horizontal/vertical, static/dynamic/smooth, true/false, 0.1-10.0").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        try {
            ConfigManager.load();
            ctx.getSource().sendSuccess(() -> Component.literal("Config reloaded!").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error reloading config: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}
