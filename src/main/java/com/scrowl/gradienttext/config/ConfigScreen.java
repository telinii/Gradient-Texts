package com.scrowl.gradienttext.config;

import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.command.ColorSuggestions;
import com.scrowl.gradienttext.gradient.GradientEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;

    private EditBox itemIdInput;
    private EditBox nameInput;
    private EditBox colorsInput;
    private EditBox directionInput;
    private EditBox modeInput;
    private EditBox speedInput;

    private Button addForcedBtn;
    private Button addBlacklistBtn;
    private Button editForcedBtn;
    private Button removeForcedBtn;
    private Button removeBlacklistBtn;
    private Button boldBtn;
    private Button toolGradientsBtn;
    private Button armorGradientsBtn;
    private Button modeStaticBtn;
    private Button modeDynamicBtn;
    private Button modeSmoothBtn;
    private Button saveBtn;
    private Button doneBtn;

    private boolean currentBold = false;
    private boolean currentToolGradients = false;
    private boolean currentArmorGradients = false;
    private String currentGradientMode = "static";

    private String statusMsg = "";
    private int statusColor = 0x55FF55;

    private final List<String> forcedList = new ArrayList<>();
    private final List<String> blacklistList = new ArrayList<>();
    private int selForced = -1;
    private int selBlacklist = -1;

    private List<String> currentSuggestions = new ArrayList<>();
    private int activeField = -1;
    private int sugSelectIndex = -1;

    private boolean editingForced = false;
    private String editingItemId = null;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Gradient Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        refreshLists();

        int lx = 20;
        int lw = 170;

        itemIdInput = new EditBox(font, lx, 55, lw, 18, Component.literal("Item ID"));
        itemIdInput.setMaxLength(128);
        itemIdInput.setValue("minecraft:diamond_sword");
        itemIdInput.setResponder(this::onItemIdChanged);
        addWidget(itemIdInput);

        nameInput = new EditBox(font, lx, 90, lw, 18, Component.literal("Custom Name"));
        nameInput.setMaxLength(64);
        nameInput.setValue("");
        nameInput.setHint(Component.literal("(optional)"));
        addWidget(nameInput);

        colorsInput = new EditBox(font, lx, 125, lw, 18, Component.literal("Colors"));
        colorsInput.setMaxLength(256);
        colorsInput.setValue("#FF0000,#0000FF");
        colorsInput.setResponder(this::onColorsChanged);
        addWidget(colorsInput);

        directionInput = new EditBox(font, lx, 160, lw, 18, Component.literal("Direction"));
        directionInput.setMaxLength(16);
        directionInput.setValue("horizontal");
        directionInput.setResponder(this::onDirectionChanged);
        addWidget(directionInput);

        modeInput = new EditBox(font, lx, 195, lw, 18, Component.literal("Mode"));
        modeInput.setMaxLength(16);
        modeInput.setValue("static");
        modeInput.setResponder(this::onModeChanged);
        addWidget(modeInput);

        speedInput = new EditBox(font, lx, 230, lw, 18, Component.literal("Speed"));
        speedInput.setMaxLength(8);
        speedInput.setValue("1.0");
        addWidget(speedInput);

        boldBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Bold: OFF"), b -> {
                    currentBold = !currentBold;
                    b.setMessage(Component.literal("Bold: " + (currentBold ? "ON" : "OFF")));
                }
        ).pos(lx, 260).size(lw, 18).build());

        int rx = 220;
        int rw = 170;

        toolGradientsBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Tool Gradients: OFF"), b -> {
                    currentToolGradients = !currentToolGradients;
                    b.setMessage(Component.literal("Tool Gradients: " + (currentToolGradients ? "ON" : "OFF")));
                }
        ).pos(rx, 55).size(rw, 18).build());

        currentToolGradients = GradientConfig.get().isDefaultToolGradients();
        toolGradientsBtn.setMessage(Component.literal("Tool Gradients: " + (currentToolGradients ? "ON" : "OFF")));

        armorGradientsBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Armor Gradients: OFF"), b -> {
                    currentArmorGradients = !currentArmorGradients;
                    b.setMessage(Component.literal("Armor Gradients: " + (currentArmorGradients ? "ON" : "OFF")));
                }
        ).pos(rx, 78).size(rw, 18).build());

        currentArmorGradients = GradientConfig.get().isDefaultArmorGradients();
        armorGradientsBtn.setMessage(Component.literal("Armor Gradients: " + (currentArmorGradients ? "ON" : "OFF")));

        int btnW = 55;
        int btnY = 101;

        modeStaticBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Static"), b -> {
                    currentGradientMode = "static";
                    updateModeButtons();
                }
        ).pos(rx, btnY).size(btnW, 18).build());

        modeDynamicBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Dynamic"), b -> {
                    currentGradientMode = "dynamic";
                    updateModeButtons();
                }
        ).pos(rx + btnW + 2, btnY).size(btnW, 18).build());

        modeSmoothBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Smooth"), b -> {
                    currentGradientMode = "smooth";
                    updateModeButtons();
                }
        ).pos(rx + (btnW + 2) * 2, btnY).size(btnW, 18).build());

        currentGradientMode = GradientConfig.get().getDefaultGradientMode();
        updateModeButtons();

        addForcedBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Add Forced"), b -> addForced()
        ).pos(lx, 280).size(lw / 2 - 2, 18).build());

        editForcedBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Edit"), b -> loadSelectedForced()
        ).pos(lx + lw / 2 + 2, 280).size(lw / 2 - 2, 18).build());

        addBlacklistBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Add Blacklist"), b -> addBlacklist()
        ).pos(lx, 305).size(lw, 18).build());

        removeForcedBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Remove Selected"), b -> removeForced()
        ).pos(rx, 260).size(rw, 18).build());

        removeBlacklistBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Remove Selected"), b -> removeBlacklist()
        ).pos(rx, 355).size(rw, 18).build());

        saveBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Save"), b -> {
                    GradientConfig.get().setDefaultToolGradients(currentToolGradients);
                    GradientConfig.get().setDefaultArmorGradients(currentArmorGradients);
                    GradientConfig.get().setDefaultGradientMode(currentGradientMode);
                    ConfigManager.save();
                    statusMsg = "Saved!";
                    statusColor = 0x55FF55;
                }
        ).pos(lx, height - 50).size(lw, 18).build());

        doneBtn = addRenderableWidget(new Button.Builder(
                CommonComponents.GUI_DONE, b -> minecraft.setScreen(parent)
        ).pos(lx, height - 26).size(lw, 18).build());
    }

    private void updateModeButtons() {
        modeStaticBtn.setMessage(Component.literal("Static" + (currentGradientMode.equals("static") ? " *" : "")));
        modeDynamicBtn.setMessage(Component.literal("Dynamic" + (currentGradientMode.equals("dynamic") ? " *" : "")));
        modeSmoothBtn.setMessage(Component.literal("Smooth" + (currentGradientMode.equals("smooth") ? " *" : "")));
    }

    private void loadSelectedForced() {
        if (selForced < 0 || selForced >= forcedList.size()) {
            statusMsg = "Select a forced gradient first";
            statusColor = 0xFF5555;
            return;
        }
        String itemId = forcedList.get(selForced);
        GradientConfig.ItemGradientEntry entry = GradientConfig.get().getForcedGradient(itemId);
        if (entry == null) return;

        itemIdInput.setValue(itemId);
        nameInput.setValue(entry.getCustomName() != null ? entry.getCustomName() : "");
        StringBuilder colors = new StringBuilder();
        for (int i = 0; i < entry.getColors().length; i++) {
            if (i > 0) colors.append(",");
            colors.append(String.format("#%06X", entry.getColors()[i]));
        }
        colorsInput.setValue(colors.toString());
        directionInput.setValue(entry.getDirection());
        modeInput.setValue(entry.getMode());
        speedInput.setValue(String.valueOf(entry.getSpeed()));

        currentBold = entry.isBold();
        boldBtn.setMessage(Component.literal("Bold: " + (currentBold ? "ON" : "OFF")));

        editingForced = true;
        editingItemId = itemId;
        addForcedBtn.setMessage(Component.literal("Update"));
        statusMsg = "Editing: " + itemId;
        statusColor = 0xFFAA00;
    }

    private void onItemIdChanged(String text) {
        if (activeField != 0) { currentSuggestions.clear(); sugSelectIndex = -1; }
        activeField = 0;
        updateItemSuggestions(text);
    }

    private void onColorsChanged(String text) {
        if (activeField != 2) { currentSuggestions.clear(); sugSelectIndex = -1; }
        activeField = 2;
        updateColorSuggestions(text);
    }

    private void onDirectionChanged(String text) {
        if (activeField != 3) { currentSuggestions.clear(); sugSelectIndex = -1; }
        activeField = 3;
        updateDirectionSuggestions(text);
    }

    private void onModeChanged(String text) {
        if (activeField != 4) { currentSuggestions.clear(); sugSelectIndex = -1; }
        activeField = 4;
        updateModeSuggestions(text);
    }

    private void updateItemSuggestions(String input) {
        currentSuggestions.clear();
        sugSelectIndex = -1;
        if (input == null || input.isEmpty()) return;
        String low = input.toLowerCase();
        int count = 0;
        for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
            if (key.toString().contains(low) && count < 8) {
                currentSuggestions.add(key.toString());
                count++;
            }
        }
    }

    private void updateColorSuggestions(String input) {
        currentSuggestions.clear();
        sugSelectIndex = -1;
        if (input == null || input.isEmpty()) return;
        String last = input;
        int c = input.lastIndexOf(',');
        if (c >= 0) last = input.substring(c + 1).trim();
        if (last.isEmpty()) return;
        String low = last.toLowerCase();
        for (String n : ColorSuggestions.NAMED_COLORS) {
            if (n.contains(low) && currentSuggestions.size() < 5) currentSuggestions.add(n);
        }
        for (String h : ColorSuggestions.COMMON_HEX) {
            if (h.toLowerCase().contains(low) && currentSuggestions.size() < 8) currentSuggestions.add(h);
        }
    }

    private void updateDirectionSuggestions(String input) {
        currentSuggestions.clear();
        sugSelectIndex = -1;
        if (input == null || input.isEmpty()) {
            currentSuggestions.add("horizontal");
            currentSuggestions.add("vertical");
            currentSuggestions.add("fix");
            return;
        }
        String low = input.toLowerCase();
        if ("horizontal".startsWith(low) || "horizontal".contains(low)) currentSuggestions.add("horizontal");
        if ("vertical".startsWith(low) || "vertical".contains(low)) currentSuggestions.add("vertical");
        if ("fix".startsWith(low) || "fix".contains(low)) currentSuggestions.add("fix");
    }

    private void updateModeSuggestions(String input) {
        currentSuggestions.clear();
        sugSelectIndex = -1;
        if (input == null || input.isEmpty()) {
            currentSuggestions.add("static");
            currentSuggestions.add("dynamic");
            currentSuggestions.add("smooth");
            return;
        }
        String low = input.toLowerCase();
        if ("static".startsWith(low) || "static".contains(low)) currentSuggestions.add("static");
        if ("dynamic".startsWith(low) || "dynamic".contains(low)) currentSuggestions.add("dynamic");
        if ("smooth".startsWith(low) || "smooth".contains(low)) currentSuggestions.add("smooth");
    }

    private void applySuggestion(String s) {
        switch (activeField) {
            case 0: itemIdInput.setValue(s); break;
            case 2:
                String cur = colorsInput.getValue();
                int c = cur.lastIndexOf(',');
                if (c >= 0) colorsInput.setValue(cur.substring(0, c + 1) + s + ",");
                else colorsInput.setValue(s + ",");
                break;
            case 3: directionInput.setValue(s); break;
            case 4: modeInput.setValue(s); break;
        }
        currentSuggestions.clear();
        sugSelectIndex = -1;
    }

    private void refreshLists() {
        forcedList.clear();
        blacklistList.clear();
        for (var e : GradientConfig.get().getForcedGradients().entrySet()) forcedList.add(e.getKey());
        for (String s : GradientConfig.get().getBlacklistedItems()) blacklistList.add(s);
    }

    private void addForced() {
        String id = itemIdInput.getValue().trim();
        if (id.isEmpty()) { statusMsg = "Enter item ID!"; statusColor = 0xFF5555; return; }
        int[] colors = parseColors(colorsInput.getValue());
        if (colors == null || colors.length < 2) { statusMsg = "Need 2+ colors!"; statusColor = 0xFF5555; return; }
        String dir = directionInput.getValue().trim();
        String mode = modeInput.getValue().trim();
        float speed = 1.0f;
        try { speed = Float.parseFloat(speedInput.getValue().trim()); } catch (Exception e) {}
        String customName = nameInput.getValue().trim();

        if (editingForced && editingItemId != null) {
            GradientConfig.get().removeForcedGradient(editingItemId);
        }

        GradientConfig.get().setForcedGradient(id, new GradientConfig.ItemGradientEntry(colors, dir, mode, currentBold, speed, customName));
        refreshLists();

        editingForced = false;
        editingItemId = null;
        addForcedBtn.setMessage(Component.literal("Add Forced"));
        statusMsg = (editingForced ? "Updated" : "Added") + ": " + id;
        statusColor = 0x55FF55;
    }

    private void addBlacklist() {
        String id = itemIdInput.getValue().trim();
        if (id.isEmpty()) { statusMsg = "Enter item ID!"; statusColor = 0xFF5555; return; }
        GradientConfig.get().addBlacklistedItem(id);
        refreshLists();
        statusMsg = "Blacklisted: " + id; statusColor = 0xFFAA00;
    }

    private void removeForced() {
        if (selForced >= 0 && selForced < forcedList.size()) {
            GradientConfig.get().removeForcedGradient(forcedList.get(selForced));
            selForced = -1; refreshLists(); statusMsg = "Removed"; statusColor = 0x55FF55;
            editingForced = false; editingItemId = null;
            addForcedBtn.setMessage(Component.literal("Add Forced"));
        } else { statusMsg = "Select an item first"; statusColor = 0xFF5555; }
    }

    private void removeBlacklist() {
        if (selBlacklist >= 0 && selBlacklist < blacklistList.size()) {
            GradientConfig.get().removeBlacklistedItem(blacklistList.get(selBlacklist));
            selBlacklist = -1; refreshLists(); statusMsg = "Removed"; statusColor = 0x55FF55;
        } else { statusMsg = "Select an item first"; statusColor = 0xFF5555; }
    }

    private int[] parseColors(String input) {
        if (input == null || input.isEmpty()) return null;
        String[] parts = input.split(",");
        List<Integer> colors = new ArrayList<>();
        for (String p : parts) {
            p = p.trim();
            if (p.isEmpty()) continue;
            try { colors.add(GradientEngine.parseColor(p)); } catch (Exception e) { return null; }
        }
        int[] r = new int[colors.size()];
        for (int i = 0; i < colors.size(); i++) r[i] = colors.get(i);
        return r;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB && !currentSuggestions.isEmpty()) {
            if (sugSelectIndex < 0) sugSelectIndex = 0;
            else sugSelectIndex = (sugSelectIndex + 1) % currentSuggestions.size();
            applySuggestion(currentSuggestions.get(sugSelectIndex));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && !currentSuggestions.isEmpty() && sugSelectIndex >= 0) {
            applySuggestion(currentSuggestions.get(sugSelectIndex));
            return true;
        }
        if (keyCode == InputConstants.KEY_ESCAPE && editingForced) {
            editingForced = false;
            editingItemId = null;
            addForcedBtn.setMessage(Component.literal("Add Forced"));
            statusMsg = "";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (itemIdInput == null || toolGradientsBtn == null) return;
        g.fill(0, 0, width, height, 0xFF000000);

        String title = editingForced ? "EDIT GRADIENT" : "ADD GRADIENT";
        g.fill(20, 18, 180, 30, 0xFF000000);
        g.drawString(font, title, 20, 20, editingForced ? 0xFFAA00 : 0x55FFFF, true);
        g.fill(20, 41, 100, 52, 0xFF000000);
        g.drawString(font, "Item ID", 20, 43, 0xAAAAAA);
        g.fill(20, 76, 130, 87, 0xFF000000);
        g.drawString(font, "Custom Name", 20, 78, 0xAAAAAA);
        g.fill(20, 111, 170, 122, 0xFF000000);
        g.drawString(font, "Colors (comma-separated)", 20, 113, 0xAAAAAA);
        g.fill(20, 146, 100, 157, 0xFF000000);
        g.drawString(font, "Direction", 20, 148, 0xAAAAAA);
        g.fill(20, 181, 70, 192, 0xFF000000);
        g.drawString(font, "Mode", 20, 183, 0xAAAAAA);
        g.fill(20, 216, 70, 227, 0xFF000000);
        g.drawString(font, "Speed", 20, 218, 0xAAAAAA);
        g.fill(20, 246, 60, 257, 0xFF000000);
        g.drawString(font, "Bold", 20, 248, 0xAAAAAA);

        g.fill(220, 18, 310, 30, 0xFF000000);
        g.drawString(font, "TOOL GRADIENTS", 220, 20, 0x55FF55, true);
        g.fill(220, 40, 350, 52, 0xFF000000);
        g.drawString(font, "Auto-gradient tools", 220, 43, 0xAAAAAA);

        g.fill(220, 100, 330, 112, 0xFF000000);
        g.drawString(font, "ARMOR GRADIENTS", 220, 102, 0x55FFFF, true);

        g.fill(220, 122, 250, 132, 0xFF000000);
        g.drawString(font, "MODE", 220, 124, 0xFF55FF, true);

        g.fill(220, 145, 280, 157, 0xFF000000);
        g.drawString(font, "FORCED", 220, 147, 0xFFAA00, true);
        int fy = 165;
        for (int i = 0; i < forcedList.size() && i < 6; i++) {
            int bg = (i == selForced) ? 0x605555FF : 0x30000000;
            g.fill(220, fy, 390, fy + 18, bg);
            g.drawString(font, forcedList.get(i), 224, fy + 5, 0xFFFFFF);
            fy += 20;
        }
        if (forcedList.isEmpty()) g.drawString(font, "(empty)", 224, fy + 4, 0x666666);

        g.fill(220, 250, 310, 262, 0xFF000000);
        g.drawString(font, "BLACKLIST", 220, 252, 0xFF5555, true);
        int by = 270;
        for (int i = 0; i < blacklistList.size() && i < 4; i++) {
            int bg = (i == selBlacklist) ? 0x60FF5555 : 0x30000000;
            g.fill(220, by, 390, by + 18, bg);
            g.drawString(font, blacklistList.get(i), 224, by + 5, 0xFFFFFF);
            by += 20;
        }
        if (blacklistList.isEmpty()) g.drawString(font, "(empty)", 224, by + 4, 0x666666);

        if (itemIdInput != null) itemIdInput.render(g, mx, my, pt);
        if (nameInput != null) nameInput.render(g, mx, my, pt);
        if (colorsInput != null) colorsInput.render(g, mx, my, pt);
        if (directionInput != null) directionInput.render(g, mx, my, pt);
        if (modeInput != null) modeInput.render(g, mx, my, pt);
        if (speedInput != null) speedInput.render(g, mx, my, pt);

        if (!currentSuggestions.isEmpty()) {
            int sx = 20;
            int sy;
            switch (activeField) {
                case 0: sy = 73; break;
                case 2: sy = 143; break;
                case 3: sy = 178; break;
                case 4: sy = 213; break;
                default: sy = 73; break;
            }
            int sw = 170;
            int sh = currentSuggestions.size() * 16 + 4;
            g.fill(sx - 1, sy - 1, sx + sw + 1, sy + sh + 1, 0xFF555555);
            g.fill(sx, sy, sx + sw, sy + sh, 0xFF101010);
            for (int i = 0; i < currentSuggestions.size(); i++) {
                int y = sy + 2 + i * 16;
                boolean selected = (i == sugSelectIndex);
                boolean hov = mx >= sx && mx <= sx + sw && my >= y && my <= y + 14;
                if (selected || hov) g.fill(sx, y, sx + sw, y + 14, 0x405555FF);
                g.drawString(font, currentSuggestions.get(i), sx + 6, y + 3, selected || hov ? 0x55FFFF : 0xCCCCCC);
            }
        }

        if (!statusMsg.isEmpty()) {
            g.fill(20, height - 70, 200, height - 64, 0xFF000000);
            g.drawString(font, statusMsg, 20, height - 68, statusColor);
        }
        g.fill(20, height - 14, 250, height - 2, 0xFF000000);
        g.drawString(font, "Tab to cycle suggestions | Esc to cancel edit", 20, height - 12, 0x555555);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && !currentSuggestions.isEmpty()) {
            int sx = 20;
            int sy;
            switch (activeField) {
                case 0: sy = 73; break;
                case 2: sy = 143; break;
                case 3: sy = 178; break;
                case 4: sy = 213; break;
                default: sy = 73; break;
            }
            int sw = 170;
            for (int i = 0; i < currentSuggestions.size(); i++) {
                int y = sy + 2 + i * 16;
                if (mx >= sx && mx <= sx + sw && my >= y && my <= y + 14) {
                    applySuggestion(currentSuggestions.get(i));
                    return true;
                }
            }
        }
        if (btn == 0) {
            for (int i = 0; i < forcedList.size() && i < 6; i++) {
                int y = 165 + i * 20;
                if (mx >= 220 && mx <= 390 && my >= y && my <= y + 18) { selForced = i; selBlacklist = -1; return true; }
            }
            for (int i = 0; i < blacklistList.size() && i < 4; i++) {
                int y = 270 + i * 20;
                if (mx >= 220 && mx <= 390 && my >= y && my <= y + 18) { selBlacklist = i; selForced = -1; return true; }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }
}
