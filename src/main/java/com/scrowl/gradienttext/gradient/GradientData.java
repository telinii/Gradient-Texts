package com.scrowl.gradienttext.gradient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GradientData {
    public static final String NBT_KEY = "GradientText";
    public static final String COLORS_KEY = "Colors";
    public static final String DIRECTION_KEY = "Direction";
    public static final String MODE_KEY = "Mode";
    public static final String BOLD_KEY = "Bold";
    public static final String SPEED_KEY = "Speed";
    public static final String ORIGINAL_NAME_KEY = "OriginalName";

    public static final float MIN_SPEED = 0.1f;
    public static final float MAX_SPEED = 10.0f;
    public static final float DEFAULT_SPEED = 1.0f;

    private int[] colors;
    private GradientDirection direction;
    private GradientMode mode;
    private boolean bold;
    private float speed;

    public GradientData(int[] colors, GradientDirection direction, GradientMode mode, boolean bold) {
        this(colors, direction, mode, bold, DEFAULT_SPEED);
    }

    public GradientData(int[] colors, GradientDirection direction, GradientMode mode, boolean bold, float speed) {
        this.colors = colors;
        this.direction = direction;
        this.mode = mode;
        this.bold = bold;
        this.speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
    }

    public int[] getColors() { return colors; }
    public void setColors(int[] colors) { this.colors = colors; }
    public GradientDirection getDirection() { return direction; }
    public void setDirection(GradientDirection direction) { this.direction = direction; }
    public GradientMode getMode() { return mode; }
    public void setMode(GradientMode mode) { this.mode = mode; }
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed)); }

    public Component applyGradient(String text) {
        return applyGradient(text, 0);
    }

    public Component applyGradient(String text, long tickOffset) {
        if (mode == GradientMode.DYNAMIC) {
            return GradientEngine.applyAnimatedGradient(text, colors, direction, bold, tickOffset, speed);
        } else if (mode == GradientMode.SMOOTH) {
            return GradientEngine.applySmoothTransition(text, colors, bold, tickOffset, speed);
        } else {
            return GradientEngine.applyGradient(text, colors, direction, bold);
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        int[] colorArray = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colorArray[i] = colors[i];
        }
        tag.putIntArray(COLORS_KEY, colorArray);

        tag.putString(DIRECTION_KEY, direction.getName());
        tag.putString(MODE_KEY, mode.getName());
        tag.putBoolean(BOLD_KEY, bold);
        tag.putFloat(SPEED_KEY, speed);

        return tag;
    }

    public static GradientData fromNBT(CompoundTag tag) {
        if (tag == null || !tag.contains(COLORS_KEY) || !tag.contains(DIRECTION_KEY)) {
            return null;
        }

        int[] colors = tag.getIntArray(COLORS_KEY);
        if (colors == null || colors.length == 0) {
            return null;
        }

        GradientDirection direction = GradientDirection.fromString(tag.getString(DIRECTION_KEY));
        GradientMode mode = GradientMode.fromString(tag.contains(MODE_KEY) ? tag.getString(MODE_KEY) : "STATIC");
        boolean bold = tag.contains(BOLD_KEY) && tag.getBoolean(BOLD_KEY);
        float speed = tag.contains(SPEED_KEY) ? tag.getFloat(SPEED_KEY) : DEFAULT_SPEED;

        return new GradientData(colors, direction, mode, bold, speed);
    }

    public static boolean hasGradient(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return stack.getTag().contains(NBT_KEY);
    }

    public static GradientData fromItemStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        CompoundTag tag = stack.getTag().getCompound(NBT_KEY);
        return fromNBT(tag);
    }

    public static void setOnItemStack(ItemStack stack, GradientData data) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(NBT_KEY)) {
            String originalName = stack.getHoverName().getString();
            tag.putString(ORIGINAL_NAME_KEY, originalName);
        }

        tag.put(NBT_KEY, data.toNBT());

        String originalName = tag.getString(ORIGINAL_NAME_KEY);
        if (originalName.isEmpty()) {
            originalName = stack.getHoverName().getString();
            tag.putString(ORIGINAL_NAME_KEY, originalName);
        }

        Component gradientName = data.applyGradient(originalName);
        stack.setHoverName(gradientName);
    }

    public static void removeFromItemStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getTag();

        if (tag.contains(ORIGINAL_NAME_KEY)) {
            String originalName = tag.getString(ORIGINAL_NAME_KEY);
            if (!originalName.isEmpty()) {
                stack.setHoverName(Component.literal(originalName));
            } else {
                stack.resetHoverName();
            }
            tag.remove(ORIGINAL_NAME_KEY);
        } else {
            stack.resetHoverName();
        }

        tag.remove(NBT_KEY);
    }
}
