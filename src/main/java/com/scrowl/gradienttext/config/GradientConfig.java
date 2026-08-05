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
    private List<GroupEntry> groups = new ArrayList<>();
    private List<ItemGradientEntry> unassigned = new ArrayList<>();

    public static final String DEFAULT_GROUP_NAME = "Default Group";
    public static final String DEFAULT_LIST_NAME = "Default List";

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
    public boolean isItemBlacklisted(String itemId) { return itemId != null && blacklistedItems.contains(itemId.toLowerCase()); }
    public void addBlacklistedItem(String itemId) { if (itemId != null) blacklistedItems.add(itemId.toLowerCase()); }
    public void removeBlacklistedItem(String itemId) { if (itemId != null) blacklistedItems.remove(itemId.toLowerCase()); }

    public List<GroupEntry> getGroups() { return groups; }
    public void setGroups(List<GroupEntry> groups) { this.groups = groups != null ? groups : new ArrayList<>(); }
    public List<ItemGradientEntry> getUnassigned() { return unassigned; }
    public void setUnassigned(List<ItemGradientEntry> unassigned) { this.unassigned = unassigned != null ? unassigned : new ArrayList<>(); }

    public GroupEntry getOrCreateDefaultGroup() {
        if (!groups.isEmpty()) return groups.get(0);
        GroupEntry group = new GroupEntry(DEFAULT_GROUP_NAME);
        group.getLists().add(new ListEntry(DEFAULT_LIST_NAME));
        groups.add(group);
        return group;
    }

    public ListEntry getOrCreateDefaultList() {
        return getOrCreateDefaultGroup().getOrCreateDefaultList();
    }

    public boolean hasAnyGradientedItems() {
        if (!unassigned.isEmpty()) return true;
        for (GroupEntry group : groups) {
            for (ListEntry list : group.getLists()) {
                if (!list.getItems().isEmpty()) return true;
            }
        }
        return false;
    }

    public List<ItemGradientEntry> getAllItems() {
        List<ItemGradientEntry> all = new ArrayList<>();
        all.addAll(unassigned);
        for (GroupEntry group : groups) {
            for (ListEntry list : group.getLists()) {
                all.addAll(list.getItems());
            }
        }
        return all;
    }

    public ItemGradientEntry getItem(String itemId) {
        if (itemId == null) return null;
        String key = itemId.toLowerCase();
        for (ItemGradientEntry entry : unassigned) {
            if (key.equals(entry.getItemId().toLowerCase())) return entry;
        }
        for (GroupEntry group : groups) {
            for (ListEntry list : group.getLists()) {
                for (ItemGradientEntry entry : list.getItems()) {
                    if (key.equals(entry.getItemId().toLowerCase())) return entry;
                }
            }
        }
        return null;
    }

    public boolean hasItem(String itemId) {
        return getItem(itemId) != null;
    }

    public static class ItemGradientEntry {
        private String itemId;
        private int[] colors;
        private String direction;
        private String mode;
        private boolean bold;
        private float speed;
        private String customName;

        public ItemGradientEntry(String itemId, int[] colors, String direction, String mode, boolean bold, float speed, String customName) {
            this.itemId = itemId != null ? itemId : "";
            this.colors = colors;
            this.direction = direction;
            this.mode = mode;
            this.bold = bold;
            this.speed = speed;
            this.customName = customName != null ? customName : "";
        }

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId != null ? itemId : ""; }
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

        public static ItemGradientEntry fromGradientData(String itemId, GradientData data) {
            return new ItemGradientEntry(itemId, data.getColors(), data.getDirection().getName(), data.getMode().getName(), data.isBold(), data.getSpeed(), "");
        }
    }

    public static class ListEntry {
        private String name;
        private List<ItemGradientEntry> items = new ArrayList<>();

        public ListEntry(String name) {
            this.name = name != null ? name : "";
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name != null ? name : ""; }
        public List<ItemGradientEntry> getItems() { return items; }
        public void setItems(List<ItemGradientEntry> items) { this.items = items != null ? items : new ArrayList<>(); }
        public ItemGradientEntry getItem(String itemId) {
            String key = itemId.toLowerCase();
            for (ItemGradientEntry e : items) {
                if (e.getItemId().toLowerCase().equals(key)) return e;
            }
            return null;
        }
        public void removeItem(String itemId) {
            items.removeIf(e -> e.getItemId().toLowerCase().equals(itemId.toLowerCase()));
        }
    }

    public static class GroupEntry {
        private String name;
        private List<ListEntry> lists = new ArrayList<>();

        public GroupEntry(String name) {
            this.name = name != null ? name : "";
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name != null ? name : ""; }
        public List<ListEntry> getLists() { return lists; }
        public void setLists(List<ListEntry> lists) { this.lists = lists != null ? lists : new ArrayList<>(); }

        public ListEntry getOrCreateDefaultList() {
            if (!lists.isEmpty()) return lists.get(0);
            ListEntry list = new ListEntry(DEFAULT_LIST_NAME);
            lists.add(list);
            return list;
        }
    }
}