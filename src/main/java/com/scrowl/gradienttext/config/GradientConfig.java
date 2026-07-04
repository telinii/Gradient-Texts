package com.scrowl.gradienttext.config;

import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientMode;

import java.util.*;

public class GradientConfig {
    private static GradientConfig INSTANCE = new GradientConfig();

    private boolean smoothGradient = true;
    private Set<String> blacklistedItems = new HashSet<>();
    private Map<String, ItemGradientEntry> forcedGradients = new LinkedHashMap<>();

    public static GradientConfig get() { return INSTANCE; }
    public static void set(GradientConfig config) { INSTANCE = config; }

    public boolean isSmoothGradient() { return smoothGradient; }
    public void setSmoothGradient(boolean smooth) { this.smoothGradient = smooth; }

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
