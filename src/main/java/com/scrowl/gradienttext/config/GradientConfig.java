package com.scrowl.gradienttext.config;

import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientMode;

import java.util.*;

public class GradientConfig {
    private static GradientConfig INSTANCE = new GradientConfig();

    private boolean smoothGradient = true;
    private boolean defaultToolGradients = false;
    private boolean defaultArmorGradients = false;
    private String defaultGradientMode = "static";
    private Set<String> blacklistedItems = new HashSet<>();
    private Map<String, ItemGradientEntry> forcedGradients = new LinkedHashMap<>();

    public static final int[] WOOD_COLORS = new int[]{0x8B4513, 0xFF8C00};
    public static final int[] STONE_COLORS = new int[]{0x808080, 0xD3D3D3};
    public static final int[] IRON_COLORS = new int[]{0xFFFFFF, 0x87CEEB};
    public static final int[] GOLD_COLORS = new int[]{0xFFD700, 0xFFA500};
    public static final int[] DIAMOND_COLORS = new int[]{0x00FFFF, 0xADD8E6};
    public static final int[] NETHERITE_COLORS = new int[]{0x555555, 0x8B0000};

    public static final int[] LEATHER_ARMOR_COLORS = new int[]{0x8B4513, 0xCD853F};
    public static final int[] CHAIN_ARMOR_COLORS = new int[]{0x999999, 0xCCCCCC};
    public static final int[] IRON_ARMOR_COLORS = new int[]{0xCCCCCC, 0xF0F0F0};
    public static final int[] GOLD_ARMOR_COLORS = new int[]{0xFFD700, 0xFFEC8B};
    public static final int[] DIAMOND_ARMOR_COLORS = new int[]{0x00CED1, 0x7FFFD4};
    public static final int[] NETHERITE_ARMOR_COLORS = new int[]{0x555555, 0x8B0000};

    public static GradientConfig get() { return INSTANCE; }
    public static void set(GradientConfig config) { INSTANCE = config; }

    public boolean isSmoothGradient() { return smoothGradient; }
    public void setSmoothGradient(boolean smooth) { this.smoothGradient = smooth; }

    public boolean isDefaultToolGradients() { return defaultToolGradients; }
    public void setDefaultToolGradients(boolean enabled) { this.defaultToolGradients = enabled; }

    public boolean isDefaultArmorGradients() { return defaultArmorGradients; }
    public void setDefaultArmorGradients(boolean enabled) { this.defaultArmorGradients = enabled; }

    public String getDefaultGradientMode() { return defaultGradientMode; }
    public void setDefaultGradientMode(String mode) { this.defaultGradientMode = mode; }

    public Set<String> getBlacklistedItems() { return blacklistedItems; }
    public void setBlacklistedItems(Set<String> items) { this.blacklistedItems = items; }
    public boolean isItemBlacklisted(String itemId) { return blacklistedItems.contains(itemId.toLowerCase()); }
    public void addBlacklistedItem(String itemId) { blacklistedItems.add(itemId.toLowerCase()); }
    public void removeBlacklistedItem(String itemId) { blacklistedItems.remove(itemId.toLowerCase()); }

    public Map<String, ItemGradientEntry> getForcedGradients() { return forcedGradients; }
    public void setForcedGradients(Map<String, ItemGradientEntry> gradients) { this.forcedGradients = gradients; }
    public ItemGradientEntry getForcedGradient(String itemId) { return forcedGradients.get(itemId.toLowerCase()); }
    public void setForcedGradient(String itemId, ItemGradientEntry entry) { forcedGradients.put(itemId.toLowerCase(), entry); }
    public void removeForcedGradient(String itemId) { forcedGradients.remove(itemId.toLowerCase()); }
    public boolean hasForcedGradient(String itemId) { return forcedGradients.containsKey(itemId.toLowerCase()); }

    public static class ItemGradientEntry {
        private int[] colors;
        private String direction;
        private String mode;
        private boolean bold;
        private float speed;
        private String customName;

        public ItemGradientEntry(int[] colors, String direction, String mode, boolean bold, float speed) {
            this(colors, direction, mode, bold, speed, "");
        }

        public ItemGradientEntry(int[] colors, String direction, String mode, boolean bold, float speed, String customName) {
            this.colors = colors;
            this.direction = direction;
            this.mode = mode;
            this.bold = bold;
            this.speed = speed;
            this.customName = customName != null ? customName : "";
        }

        public int[] getColors() { return colors; }
        public void setColors(int[] colors) { this.colors = colors; }
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public boolean isBold() { return bold; }
        public void setBold(boolean bold) { this.bold = bold; }
        public float getSpeed() { return speed; }
        public void setSpeed(float speed) { this.speed = speed; }
        public String getCustomName() { return customName; }
        public void setCustomName(String customName) { this.customName = customName != null ? customName : ""; }
        public boolean hasCustomName() { return customName != null && !customName.isEmpty(); }

        public GradientData toGradientData() {
            GradientDirection dir = GradientDirection.fromString(direction);
            GradientMode m = GradientMode.fromString(mode);
            return new GradientData(colors, dir, m, bold, speed);
        }

        public static ItemGradientEntry fromGradientData(GradientData data) {
            return new ItemGradientEntry(
                    data.getColors(),
                    data.getDirection().getName(),
                    data.getMode().getName(),
                    data.isBold(),
                    data.getSpeed(),
                    ""
            );
        }
    }
}
