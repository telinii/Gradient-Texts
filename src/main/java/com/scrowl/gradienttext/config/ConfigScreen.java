package com.scrowl.gradienttext.config;

import com.mojang.blaze3d.platform.InputConstants;
import com.scrowl.gradienttext.command.ColorSuggestions;
import com.scrowl.gradienttext.gradient.GradientEngine;
import com.scrowl.gradienttext.render.BackgroundPatterns;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;

    // ---- form inputs ----
    private EditBox itemIdInput;
    private EditBox nameInput;
    private EditBox colorsInput;
    private EditBox directionInput;
    private EditBox modeInput;
    private EditBox speedInput;
    private boolean currentBold = false;

    // ---- header ----
    private Button toolGradientsBtn;
    private Button armorGradientsBtn;
    private Button materialGradientsBtn;
    private Button exportPresetBtn;
    private Button importPresetBtn;
    private Button modeStaticBtn;
    private Button modeDynamicBtn;
    private Button modeSmoothBtn;
    private String currentGradientMode = "static";

    // ---- left toolbar ----
    private Button tabListsBtn;
    private Button tabBlacklistBtn;
    private Button patternTabBtn;
    private Button addGroupBtn;
    private Button addListBtn;
    private Button renameBtn;
    private Button deleteBtn;
    private Button moveListBtn;
    private Button groupUpBtn;
    private Button groupDownBtn;

    // ---- center toolbar ----
    private Button itemEditBtn;
    private Button itemMoveBtn;
    private Button itemRemoveBtn;

    // ---- form buttons ----
    private Button boldBtn;
    private Button addBtn;
    private Button cancelEditBtn;
    private Button addBlacklistBtn;
    private Button saveBtn;
    private Button doneBtn;

    // ---- popup ----
    private EditBox popupInput;

    // ---- state ----
    private boolean showBlacklistTab = false;
    private boolean showPatternTab = false;
    private String currentBackgroundPattern = "deepslate_vein";
    private int selGroupIndex = -1;
    private int selListIndex = -1;
    private boolean viewUnassigned = false;
    private int selItemIndex = -1;
    private int selBlacklistIndex = -1;

    private boolean editing = false;
    private String editingListKey = "";

    private String statusMsg = "";
    private int statusColor = 0x55FF55;

    private int scrollLeft = 0;
    private int scrollItems = 0;

    private List<String> currentSuggestions = new ArrayList<>();
    private int activeField = -1;
    private int sugSelectIndex = -1;

    // ---- popup state ----
    private enum Popup { NONE, ADD_CHOICE, NAME_INPUT, CONFIRM_DELETE, PICK_LIST, PICK_GROUP, PICK_PRESET }
    private Popup popup = Popup.NONE;
    private String popupTitle = "";
    private String popupPrompt = "";
    private int popupScroll = 0;
    private boolean popupTargetIsGroup = false;
    private GradientConfig.GroupEntry popupTargetGroup = null;
    private GradientConfig.ListEntry popupTargetList = null;
    private GradientConfig.ItemGradientEntry popupTargetItem = null;
    private boolean popupConfirmMove = false;
    private int pendingNameAction = 0; // 0 none, 1 addList, 2 groupName, 3 listName, 4 renameGroup, 5 renameList
    private final List<String> popupPresets = new ArrayList<>();
    private final List<int[]> presetRows = new ArrayList<>();

    // ---- row geometries (rebuilt each frame) ----
    private final List<int[]> leftRows = new ArrayList<>();
    private final List<int[]> itemRows = new ArrayList<>();
    private final List<PopupRow> popupRows = new ArrayList<>();

    private static class PopupRow {
        int x, y, w, h;
        final GradientConfig.GroupEntry group;
        final GradientConfig.ListEntry list;
        PopupRow(int x, int y, int w, int h, GradientConfig.GroupEntry group, GradientConfig.ListEntry list) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.group = group; this.list = list;
        }
    }

    // ---- layout metrics ----
    private int margin = 8;
    private int headerH = 54;
    private int leftW = 176;
    private int rightW = 220;
    private int leftX, rightX, centerX, centerW;
    private int contentTop, contentBottom;

    private int leftListTop() { return contentTop + 16 + 2 + 16 + 2 + 16 + 4; }
    private int centerListTop() { return contentTop + 22 + 2 + 20 + 2; }

    public ConfigScreen(Screen parent) {
        super(Component.literal("Gradient Config"));
        this.parent = parent;
    }

    // ================================================================
    // INIT
    // ================================================================
    @Override
    protected void init() {
        super.init();
        computeLayout();

        refreshFormFromConfigDefaults();

        // ---- header toggles ----
        toolGradientsBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Tool Gradients: OFF"), b -> {
                    currentToolGradients = !currentToolGradients;
                    updateToggleButtons();
                }
        ).pos(margin, contentTop - 34).size(96, 16).build());

        armorGradientsBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Armor: OFF"), b -> {
                    currentArmorGradients = !currentArmorGradients;
                    updateToggleButtons();
                }
        ).pos(margin + 100, contentTop - 34).size(90, 16).build());

        materialGradientsBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Material Gradients: OFF"), b -> {
                    currentMaterialGradients = !currentMaterialGradients;
                    updateToggleButtons();
                }
        ).pos(margin, contentTop - 18).size(170, 16).build());

        exportPresetBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Export"), b -> onExportPreset()
        ).pos(margin + 174, contentTop - 18).size(58, 16).build());

        importPresetBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Import"), b -> onImportPreset()
        ).pos(margin + 236, contentTop - 18).size(58, 16).build());

        modeStaticBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Static"), b -> {
                    currentGradientMode = "static";
                    updateModeButtons();
                }
        ).pos(margin + 200, contentTop - 34).size(50, 16).build());

        modeDynamicBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Dynamic"), b -> {
                    currentGradientMode = "dynamic";
                    updateModeButtons();
                }
        ).pos(margin + 254, contentTop - 34).size(58, 16).build());

        modeSmoothBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Smooth"), b -> {
                    currentGradientMode = "smooth";
                    updateModeButtons();
                }
        ).pos(margin + 316, contentTop - 34).size(54, 16).build());

        updateToggleButtons();
        updateModeButtons();

        // ---- form inputs ----
        int fy = contentTop;
        itemIdInput = new EditBox(font, rightX, fy, rightW, 16, Component.literal("Item ID"));
        itemIdInput.setMaxLength(128);
        itemIdInput.setResponder(this::onItemIdChanged);
        addWidget(itemIdInput);

        nameInput = new EditBox(font, rightX, fy + 30, rightW, 16, Component.literal("Custom Name"));
        nameInput.setMaxLength(64);
        nameInput.setHint(Component.literal("(optional)"));
        addWidget(nameInput);

        colorsInput = new EditBox(font, rightX, fy + 60, rightW, 16, Component.literal("Colors"));
        colorsInput.setMaxLength(256);
        colorsInput.setValue("#FF0000,#0000FF");
        colorsInput.setResponder(this::onColorsChanged);
        addWidget(colorsInput);

        directionInput = new EditBox(font, rightX, fy + 90, rightW, 16, Component.literal("Direction"));
        directionInput.setMaxLength(16);
        directionInput.setValue("horizontal");
        directionInput.setResponder(this::onDirectionChanged);
        addWidget(directionInput);

        modeInput = new EditBox(font, rightX, fy + 120, rightW, 16, Component.literal("Mode"));
        modeInput.setMaxLength(16);
        modeInput.setValue("static");
        modeInput.setResponder(this::onModeChanged);
        addWidget(modeInput);

        speedInput = new EditBox(font, rightX, fy + 150, rightW, 16, Component.literal("Speed"));
        speedInput.setMaxLength(8);
        speedInput.setValue("1.0");
        addWidget(speedInput);

        boldBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Bold: OFF"), b -> {
                    currentBold = !currentBold;
                    b.setMessage(Component.literal("Bold: " + (currentBold ? "ON" : "OFF")));
                }
        ).pos(rightX, fy + 168).size(rightW, 18).build());

        addBtn = addRenderableWidget(new Button.Builder(
                Component.literal("ADD"), b -> onAddPressed()
        ).pos(rightX, fy + 190).size(rightW, 20).build());

        cancelEditBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Cancel Edit"), b -> cancelEditing()
        ).pos(rightX, fy + 212).size(rightW, 16).build());

        addBlacklistBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Add to Blacklist"), b -> addToBlacklistFromForm()
        ).pos(rightX, fy + 232).size(rightW, 16).build());

        saveBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Save Config"), b -> {
                    saveHeaderSettings();
                    statusMsg = "Saved!";
                    statusColor = 0x55FF55;
                }
        ).pos(rightX, fy + 254).size(rightW, 18).build());

        doneBtn = addRenderableWidget(new Button.Builder(
                CommonComponents.GUI_DONE, b -> minecraft.setScreen(parent)
        ).pos(rightX, fy + 276).size(rightW, 18).build());

        // ---- left tabs + toolbar ----
        int tabW = (leftW - 8) / 3;
        tabListsBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Lists"), b -> {
                    showBlacklistTab = false;
                    showPatternTab = false;
                    updateTabButtons();
                    selItemIndex = -1;
                }
        ).pos(leftX, contentTop).size(tabW, 16).build());

        tabBlacklistBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Blacklist"), b -> {
                    showBlacklistTab = true;
                    showPatternTab = false;
                    updateTabButtons();
                    selItemIndex = -1;
                }
        ).pos(leftX + tabW + 4, contentTop).size(tabW, 16).build());

        patternTabBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Pattern"), b -> {
                    showPatternTab = true;
                    showBlacklistTab = false;
                    updateTabButtons();
                    selItemIndex = -1;
                }
        ).pos(leftX + (tabW + 4) * 2, contentTop).size(tabW, 16).build());

        addGroupBtn = addRenderableWidget(new Button.Builder(
                Component.literal("+Group"), b -> startNamePrompt(1)
        ).pos(leftX, contentTop + 18).size(leftW / 2 - 2, 16).build());

        addListBtn = addRenderableWidget(new Button.Builder(
                Component.literal("+List"), b -> startNamePrompt(2)
        ).pos(leftX + leftW / 2 + 2, contentTop + 18).size(leftW / 2 - 2, 16).build());

        renameBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Ren"), b -> onRename()
        ).pos(leftX, contentTop + 36).size(27, 16).build());

        deleteBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Del"), b -> onDelete()
        ).pos(leftX + 29, contentTop + 36).size(27, 16).build());

        moveListBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Mv"), b -> onMoveList()
        ).pos(leftX + 58, contentTop + 36).size(27, 16).build());

        groupUpBtn = addRenderableWidget(new Button.Builder(
                Component.literal("▲"), b -> onGroupUp()
        ).pos(leftX + 87, contentTop + 36).size(27, 16).build());

        groupDownBtn = addRenderableWidget(new Button.Builder(
                Component.literal("▼"), b -> onGroupDown()
        ).pos(leftX + 116, contentTop + 36).size(27, 16).build());

        // ---- center toolbar ----
        int tw = (centerW - 8) / 3;
        itemEditBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Edit"), b -> loadSelectedItem()
        ).pos(centerX, contentTop + 18).size(tw, 20).build());

        itemMoveBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Move"), b -> onMoveItem()
        ).pos(centerX + tw + 4, contentTop + 18).size(tw, 20).build());

        itemRemoveBtn = addRenderableWidget(new Button.Builder(
                Component.literal("Remove"), b -> onRemoveItem()
        ).pos(centerX + (tw + 4) * 2, contentTop + 18).size(tw, 20).build());

        // ---- popup input ----
        popupInput = new EditBox(font, 0, 0, 200, 16, Component.literal(""));
        popupInput.setMaxLength(128);
        addWidget(popupInput);

        updateTabButtons();
    }

    private boolean currentToolGradients = false;
    private boolean currentArmorGradients = false;
    private boolean currentMaterialGradients = false;

    private void computeLayout() {
        leftX = margin;
        rightX = width - margin - rightW;
        centerX = leftX + leftW + 6;
        centerW = rightX - centerX - 6;
        if (centerW < 100) {
            rightW = Math.max(120, width - centerX - 120);
            rightX = width - margin - rightW;
            centerW = rightX - centerX - 6;
        }
        contentTop = headerH + 4;
        contentBottom = height - margin;
    }

    private void refreshFormFromConfigDefaults() {
        currentToolGradients = GradientConfig.get().isDefaultToolGradients();
        currentArmorGradients = GradientConfig.get().isDefaultArmorGradients();
        currentMaterialGradients = GradientConfig.get().isDefaultMaterialGradients();
        currentGradientMode = GradientConfig.get().getDefaultGradientMode();
        currentBackgroundPattern = GradientConfig.get().getBackgroundPattern();
    }

    private void updateToggleButtons() {
        toolGradientsBtn.setMessage(Component.literal("Tool Gradients: " + (currentToolGradients ? "ON" : "OFF")));
        armorGradientsBtn.setMessage(Component.literal("Armor: " + (currentArmorGradients ? "ON" : "OFF")));
        materialGradientsBtn.setMessage(Component.literal("Material Gradients: " + (currentMaterialGradients ? "ON" : "OFF")));
    }

    private void updateModeButtons() {
        modeStaticBtn.setMessage(Component.literal("Static" + (currentGradientMode.equals("static") ? " *" : "")));
        modeDynamicBtn.setMessage(Component.literal("Dynamic" + (currentGradientMode.equals("dynamic") ? " *" : "")));
        modeSmoothBtn.setMessage(Component.literal("Smooth" + (currentGradientMode.equals("smooth") ? " *" : "")));
    }

    private void updateTabButtons() {
        tabListsBtn.setMessage(Component.literal("Lists" + (!showBlacklistTab && !showPatternTab ? " *" : "")));
        tabBlacklistBtn.setMessage(Component.literal("Blacklist" + (showBlacklistTab ? " *" : "")));
        patternTabBtn.setMessage(Component.literal("Pattern" + (showPatternTab ? " *" : "")));
    }

    private void saveHeaderSettings() {
        GradientConfig.get().setDefaultToolGradients(currentToolGradients);
        GradientConfig.get().setDefaultArmorGradients(currentArmorGradients);
        GradientConfig.get().setDefaultMaterialGradients(currentMaterialGradients);
        GradientConfig.get().setDefaultGradientMode(currentGradientMode);
        GradientConfig.get().setBackgroundPattern(currentBackgroundPattern);
        ConfigManager.save();
    }

    private void onExportPreset() {
        if (!GradientConfig.get().hasAnyGradientedItems()) {
            statusMsg = "Nothing to export yet - add items first";
            statusColor = 0xFFAA00;
            return;
        }
        saveHeaderSettings();
        startNamePrompt(6);
    }

    private void onImportPreset() {
        popupPresets.clear();
        popupPresets.addAll(ConfigManager.listPresets());
        popupScroll = 0;
        popup = Popup.PICK_PRESET;
        popupTitle = "Import preset:";
    }

    private void doImportPreset(String fileName) {
        if (ConfigManager.importPreset(fileName)) {
            popup = Popup.NONE;
            refreshFormFromConfigDefaults();
            selGroupIndex = -1;
            selListIndex = -1;
            selItemIndex = -1;
            selBlacklistIndex = -1;
            viewUnassigned = false;
            updateToggleButtons();
            updateModeButtons();
            statusMsg = "Imported: " + fileName;
            statusColor = 0x55FF55;
        } else {
            statusMsg = "Import failed: " + fileName;
            statusColor = 0xFF5555;
        }
    }

    // ================================================================
    // SELECTION HELPERS
    // ================================================================
    private GradientConfig.GroupEntry getSelectedGroup() {
        if (selGroupIndex >= 0 && selGroupIndex < GradientConfig.get().getGroups().size()) {
            return GradientConfig.get().getGroups().get(selGroupIndex);
        }
        return null;
    }

    private GradientConfig.ListEntry getSelectedList() {
        GradientConfig.GroupEntry group = getSelectedGroup();
        if (group != null && selListIndex >= 0 && selListIndex < group.getLists().size()) {
            return group.getLists().get(selListIndex);
        }
        return null;
    }

    private GradientConfig.GroupEntry defaultGroup() {
        return GradientConfig.get().getOrCreateDefaultGroup();
    }

    // ================================================================
    // ROW GEOMETRY
    // ================================================================
    private void buildRows() {
        leftRows.clear();
        itemRows.clear();
        popupRows.clear();

        // --- left panel rows ---
        if (showPatternTab) {
            int y = leftListTop();
            List<BackgroundPatterns.Pattern> pats = BackgroundPatterns.getAll();
            for (int i = 0; i < pats.size(); i++) {
                leftRows.add(new int[]{leftX + 2, y, leftW - 4, 22, 3, i, -1});
                y += 22;
            }
            int maxScroll = y - contentBottom;
            if (maxScroll > 0) {
                scrollLeft = Math.max(0, Math.min(maxScroll, scrollLeft));
                for (int[] r : leftRows) r[1] -= scrollLeft;
            } else {
                scrollLeft = 0;
            }
        } else if (!showBlacklistTab) {
            int y = leftListTop();
            List<GradientConfig.GroupEntry> groups = GradientConfig.get().getGroups();
            for (int gi = 0; gi < groups.size(); gi++) {
                leftRows.add(new int[]{leftX + 2, y, leftW - 4, 16, 0, gi, -1});
                y += 16;
                if (gi == selGroupIndex) {
                    List<GradientConfig.ListEntry> lists = groups.get(gi).getLists();
                    for (int li = 0; li < lists.size(); li++) {
                        leftRows.add(new int[]{leftX + 10, y, leftW - 12, 14, 1, gi, li});
                        y += 14;
                    }
                }
            }
            leftRows.add(new int[]{leftX + 2, y, leftW - 4, 16, 2, -1, -1}); // Unassigned
            y += 16;

            int maxScroll = y - contentBottom;
            if (maxScroll > 0) {
                scrollLeft = Math.max(0, Math.min(maxScroll, scrollLeft));
                for (int[] r : leftRows) r[1] -= scrollLeft;
            } else {
                scrollLeft = 0;
            }
        }

        // --- center rows ---
        if (showBlacklistTab) {
            List<String> bl = new ArrayList<>(GradientConfig.get().getBlacklistedItems());
            for (int i = 0; i < bl.size(); i++) {
                itemRows.add(new int[]{centerX, centerListTop() + i * 18, centerW, 18, 0, i, -1});
            }
            int maxScroll = bl.size() * 18 - (contentBottom - centerListTop());
            if (maxScroll > 0) {
                scrollItems = Math.max(0, Math.min(maxScroll, scrollItems));
                for (int[] r : itemRows) r[1] -= scrollItems;
            } else {
                scrollItems = 0;
            }
        } else {
            List<GradientConfig.ItemGradientEntry> items = getViewItems();
            for (int i = 0; i < items.size(); i++) {
                itemRows.add(new int[]{centerX, centerListTop() + i * 18, centerW, 18, 0, i, -1});
            }
            int maxScroll = items.size() * 18 - (contentBottom - centerListTop());
            if (maxScroll > 0) {
                scrollItems = Math.max(0, Math.min(maxScroll, scrollItems));
                for (int[] r : itemRows) r[1] -= scrollItems;
            } else {
                scrollItems = 0;
            }
        }

        // --- popup rows (target list / group pickers) ---
        if (popup == Popup.PICK_LIST) {
            int y = popupListTop();
            for (GradientConfig.GroupEntry g : GradientConfig.get().getGroups()) {
                for (GradientConfig.ListEntry l : g.getLists()) {
                    popupRows.add(new PopupRow(popupX() + 12, y, popupW() - 24, 14, g, l));
                    y += 14;
                }
            }
            popupRows.add(new PopupRow(popupX() + 12, y, popupW() - 24, 14, null, null)); // Unassigned
            y += 16;
            int maxScroll = y - popupBottom();
            if (maxScroll > 0) {
                popupScroll = Math.max(0, Math.min(maxScroll, popupScroll));
                for (PopupRow r : popupRows) r.y -= popupScroll;
            } else {
                popupScroll = 0;
            }
        } else if (popup == Popup.PICK_GROUP) {
            int y = popupListTop();
            for (int gi = 0; gi < GradientConfig.get().getGroups().size(); gi++) {
                GradientConfig.GroupEntry g = GradientConfig.get().getGroups().get(gi);
                popupRows.add(new PopupRow(popupX() + 12, y, popupW() - 24, 14, g, null));
                y += 14;
            }
            int maxScroll = y - popupBottom();
            if (maxScroll > 0) {
                popupScroll = Math.max(0, Math.min(maxScroll, popupScroll));
                for (PopupRow r : popupRows) r.y -= popupScroll;
            } else {
                popupScroll = 0;
            }
        } else if (popup == Popup.PICK_PRESET) {
            int y = popupListTop();
            presetRows.clear();
            for (int i = 0; i < popupPresets.size(); i++) {
                presetRows.add(new int[]{popupX() + 12, y, popupW() - 24, 14});
                y += 14;
            }
            int maxScroll = y - popupBottom();
            if (maxScroll > 0) {
                popupScroll = Math.max(0, Math.min(maxScroll, popupScroll));
                for (int[] r : presetRows) r[1] -= popupScroll;
            } else {
                popupScroll = 0;
            }
        }
    }

    private List<GradientConfig.ItemGradientEntry> getViewItems() {
        if (viewUnassigned) {
            return GradientConfig.get().getUnassigned();
        }
        GradientConfig.ListEntry list = getSelectedList();
        if (list != null) {
            return list.getItems();
        }
        return new ArrayList<>();
    }

    // ================================================================
    // FORM LOGIC
    // ================================================================
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

    private String colorSummary(int[] colors) {
        if (colors == null || colors.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < colors.length && i < 3; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("#%06X", colors[i]));
        }
        if (colors.length > 3) sb.append("...");
        return sb.toString();
    }

    private GradientConfig.ItemGradientEntry buildEntryFromForm() {
        String id = itemIdInput.getValue().trim();
        if (id.isEmpty()) {
            statusMsg = "Enter item ID!";
            statusColor = 0xFF5555;
            return null;
        }
        int[] colors = parseColors(colorsInput.getValue());
        if (colors == null || colors.length < 2) {
            statusMsg = "Need 2+ colors!";
            statusColor = 0xFF5555;
            return null;
        }
        String dir = directionInput.getValue().trim();
        String mode = modeInput.getValue().trim();
        float speed = 1.0f;
        try { speed = Float.parseFloat(speedInput.getValue().trim()); } catch (Exception ignored) {}
        String customName = nameInput.getValue().trim();
        return new GradientConfig.ItemGradientEntry(id, colors, dir, mode, currentBold, speed, customName);
    }

    private void onAddPressed() {
        if (editing) {
            GradientConfig.ItemGradientEntry entry = getSelectedViewItem();
            if (entry != null) {
                updateFormIntoEntry(entry);
                ConfigManager.save();
                statusMsg = "Updated: " + entry.getItemId();
                statusColor = 0x55FF55;
                selItemIndex = -1;
            }
            cancelEditing();
            return;
        }

        GradientConfig.ItemGradientEntry entry = buildEntryFromForm();
        if (entry == null) return;

        if (showBlacklistTab) {
            GradientConfig.get().addBlacklistedItem(entry.getItemId());
            ConfigManager.save();
            statusMsg = "Blacklisted: " + entry.getItemId();
            statusColor = 0xFFAA00;
            selBlacklistIndex = -1;
            return;
        }

        if (!viewUnassigned && selListIndex >= 0) {
            GradientConfig.ListEntry list = getSelectedList();
            if (list != null) {
                list.getItems().add(entry);
                ConfigManager.save();
                statusMsg = "Added to " + list.getName() + ": " + entry.getItemId();
                statusColor = 0x55FF55;
                selItemIndex = list.getItems().size() - 1;
                return;
            }
        }

        // no list selected -> popup choice
        openAddChoice(entry);
    }

    private GradientConfig.ItemGradientEntry pendingAddEntry;

    private void openAddChoice(GradientConfig.ItemGradientEntry entry) {
        pendingAddEntry = entry;
        popup = Popup.ADD_CHOICE;
        popupTitle = "Add item to a list?";
        popupScroll = 0;
    }

    private void finishAddToList(GradientConfig.ListEntry list) {
        if (pendingAddEntry == null) return;
        list.getItems().add(pendingAddEntry);
        ConfigManager.save();
        statusMsg = "Added to " + list.getName() + ": " + pendingAddEntry.getItemId();
        statusColor = 0x55FF55;
        selItemIndex = list.getItems().size() - 1;
        popup = Popup.NONE;
    }

    private void finishAddToUnassigned() {
        if (pendingAddEntry == null) return;
        GradientConfig.get().getUnassigned().add(pendingAddEntry);
        ConfigManager.save();
        statusMsg = "Added (Unassigned): " + pendingAddEntry.getItemId();
        statusColor = 0x55FF55;
        popup = Popup.NONE;
    }

    private void onRename() {
        if (showBlacklistTab) return;
        GradientConfig.ListEntry list = getSelectedList();
        if (list != null) {
            popupTargetList = list;
            popupTargetIsGroup = false;
            startNamePrompt(4);
            return;
        }
        GradientConfig.GroupEntry group = getSelectedGroup();
        if (group != null) {
            popupTargetGroup = group;
            popupTargetIsGroup = true;
            startNamePrompt(5);
            return;
        }
        statusMsg = "Select a list or group first";
        statusColor = 0xFF5555;
    }

    private void onDelete() {
        if (showBlacklistTab) {
            if (selBlacklistIndex >= 0) {
                String id = new ArrayList<>(GradientConfig.get().getBlacklistedItems()).get(selBlacklistIndex);
                GradientConfig.get().removeBlacklistedItem(id);
                ConfigManager.save();
                selBlacklistIndex = -1;
                statusMsg = "Removed from blacklist: " + id;
                statusColor = 0x55FF55;
            } else {
                statusMsg = "Select a blacklist entry first";
                statusColor = 0xFF5555;
            }
            return;
        }

        GradientConfig.ListEntry list = getSelectedList();
        if (list != null) {
            popupTargetList = list;
            popupTargetIsGroup = false;
            popup = Popup.CONFIRM_DELETE;
            popupTitle = "Delete list \"" + list.getName() + "\"?";
            return;
        }
        GradientConfig.GroupEntry group = getSelectedGroup();
        if (group != null) {
            popupTargetGroup = group;
            popupTargetIsGroup = true;
            popup = Popup.CONFIRM_DELETE;
            popupTitle = "Delete group \"" + group.getName() + "\"?";
            return;
        }
        statusMsg = "Select a list or group first";
        statusColor = 0xFF5555;
    }

    private void doDeleteConfirmed() {
        if (popupTargetIsGroup) {
            GradientConfig.get().getGroups().remove(popupTargetGroup);
            selGroupIndex = -1;
            selListIndex = -1;
        } else {
            GradientConfig.GroupEntry group = getSelectedGroup();
            if (group != null) {
                group.getLists().remove(popupTargetList);
            }
            selListIndex = -1;
        }
        selItemIndex = -1;
        ConfigManager.save();
        popup = Popup.NONE;
        statusMsg = "Deleted";
        statusColor = 0x55FF55;
    }

    private void onMoveList() {
        if (showBlacklistTab) return;
        GradientConfig.GroupEntry group = getSelectedGroup();
        if (group == null) {
            statusMsg = "Select a group containing a list first";
            statusColor = 0xFF5555;
            return;
        }
        if (group.getLists().size() <= selListIndex || selListIndex < 0) {
            statusMsg = "Select a list to move";
            statusColor = 0xFF5555;
            return;
        }
        if (GradientConfig.get().getGroups().size() < 2) {
            statusMsg = "Need 2+ groups to move a list";
            statusColor = 0xFFAA00;
            return;
        }
        popup = Popup.PICK_GROUP;
        popupTitle = "Move \"" + group.getLists().get(selListIndex).getName() + "\" to group:";
        popupScroll = 0;
    }

    private void onGroupUp() {
        if (showBlacklistTab) return;
        if (selListIndex >= 0 && getSelectedList() != null) {
            moveSelectedListBy(-1);
            return;
        }
        List<GradientConfig.GroupEntry> groups = GradientConfig.get().getGroups();
        if (selGroupIndex > 0) {
            java.util.Collections.swap(groups, selGroupIndex, selGroupIndex - 1);
            selGroupIndex--;
            ConfigManager.save();
        }
    }

    private void onGroupDown() {
        if (showBlacklistTab) return;
        if (selListIndex >= 0 && getSelectedList() != null) {
            moveSelectedListBy(1);
            return;
        }
        List<GradientConfig.GroupEntry> groups = GradientConfig.get().getGroups();
        if (selGroupIndex >= 0 && selGroupIndex < groups.size() - 1) {
            java.util.Collections.swap(groups, selGroupIndex, selGroupIndex + 1);
            selGroupIndex++;
            ConfigManager.save();
        }
    }

    private void moveSelectedListBy(int dir) {
        GradientConfig.GroupEntry group = getSelectedGroup();
        if (group == null || selListIndex < 0) return;
        int target = selListIndex + dir;
        if (target < 0 || target >= group.getLists().size()) return;
        java.util.Collections.swap(group.getLists(), selListIndex, target);
        selListIndex = target;
        ConfigManager.save();
    }

    private void onMoveItem() {
        if (showBlacklistTab) return;
        GradientConfig.ItemGradientEntry entry = getSelectedViewItem();
        if (entry == null) {
            statusMsg = "Select an item first";
            statusColor = 0xFF5555;
            return;
        }
        if (totalListCount() + (GradientConfig.get().getUnassigned().isEmpty() ? 0 : 1) < 2) {
            statusMsg = "No other list to move into";
            statusColor = 0xFFAA00;
            return;
        }
        popupTargetItem = entry;
        popup = Popup.PICK_LIST;
        popupTitle = "Move item into:";
        popupScroll = 0;
    }

    private GradientConfig.ItemGradientEntry getSelectedViewItem() {
        List<GradientConfig.ItemGradientEntry> items = getViewItems();
        if (selItemIndex >= 0 && selItemIndex < items.size()) {
            return items.get(selItemIndex);
        }
        return null;
    }

    private int totalListCount() {
        int n = 0;
        for (GradientConfig.GroupEntry g : GradientConfig.get().getGroups()) n += g.getLists().size();
        return n;
    }

    private void doMoveItem(GradientConfig.ListEntry targetList, boolean toUnassigned) {
        if (popupTargetItem == null) return;
        removeItemFromCurrentLocation(popupTargetItem);
        if (toUnassigned) {
            GradientConfig.get().getUnassigned().add(popupTargetItem);
        } else if (targetList != null) {
            targetList.getItems().add(popupTargetItem);
        }
        ConfigManager.save();
        selItemIndex = -1;
        popup = Popup.NONE;
        statusMsg = "Moved: " + popupTargetItem.getItemId();
        statusColor = 0x55FF55;
    }

    private void removeItemFromCurrentLocation(GradientConfig.ItemGradientEntry entry) {
        GradientConfig.get().getUnassigned().remove(entry);
        for (GradientConfig.GroupEntry g : GradientConfig.get().getGroups()) {
            for (GradientConfig.ListEntry l : g.getLists()) {
                l.getItems().remove(entry);
            }
        }
    }

    private void onRemoveItem() {
        if (showBlacklistTab) {
            if (selBlacklistIndex >= 0) {
                String id = new ArrayList<>(GradientConfig.get().getBlacklistedItems()).get(selBlacklistIndex);
                GradientConfig.get().removeBlacklistedItem(id);
                ConfigManager.save();
                selBlacklistIndex = -1;
                statusMsg = "Removed from blacklist: " + id;
                statusColor = 0x55FF55;
            }
            return;
        }
        GradientConfig.ItemGradientEntry entry = getSelectedViewItem();
        if (entry == null) {
            statusMsg = "Select an item first";
            statusColor = 0xFF5555;
            return;
        }
        removeItemFromCurrentLocation(entry);
        ConfigManager.save();
        selItemIndex = -1;
        statusMsg = "Removed: " + entry.getItemId();
        statusColor = 0x55FF55;
    }

    private void loadSelectedItem() {
        if (showBlacklistTab) return;
        GradientConfig.ItemGradientEntry entry = getSelectedViewItem();
        if (entry == null) {
            statusMsg = "Select an item first";
            statusColor = 0xFF5555;
            return;
        }
        itemIdInput.setValue(entry.getItemId());
        nameInput.setValue(entry.getCustomName() != null ? entry.getCustomName() : "");
        colorsInput.setValue(colorSummary(entry.getColors()));
        directionInput.setValue(entry.getDirection());
        modeInput.setValue(entry.getMode());
        speedInput.setValue(String.valueOf(entry.getSpeed()));
        currentBold = entry.isBold();
        boldBtn.setMessage(Component.literal("Bold: " + (currentBold ? "ON" : "OFF")));
        editing = true;
        addBtn.setMessage(Component.literal("UPDATE"));
        statusMsg = "Editing: " + entry.getItemId();
        statusColor = 0xFFAA00;
    }

    private void cancelEditing() {
        editing = false;
        addBtn.setMessage(Component.literal("ADD"));
        statusMsg = "";
    }

    private void updateFormIntoEntry(GradientConfig.ItemGradientEntry entry) {
        String id = itemIdInput.getValue().trim();
        int[] colors = parseColors(colorsInput.getValue());
        if (colors == null || colors.length < 2) {
            statusMsg = "Need 2+ colors!";
            statusColor = 0xFF5555;
            return;
        }
        entry.setItemId(id);
        entry.setColors(colors);
        entry.setDirection(directionInput.getValue().trim());
        entry.setMode(modeInput.getValue().trim());
        entry.setBold(currentBold);
        try { entry.setSpeed(Float.parseFloat(speedInput.getValue().trim())); } catch (Exception ignored) {}
        entry.setCustomName(nameInput.getValue().trim());
    }

    private void addToBlacklistFromForm() {
        String id = itemIdInput.getValue().trim();
        if (id.isEmpty()) {
            statusMsg = "Enter item ID!";
            statusColor = 0xFF5555;
            return;
        }
        GradientConfig.get().addBlacklistedItem(id);
        ConfigManager.save();
        statusMsg = "Blacklisted: " + id;
        statusColor = 0xFFAA00;
    }

    // ================================================================
    // NAME PROMPT
    // ================================================================
    private void startNamePrompt(int action) {
        pendingNameAction = action;
        popup = Popup.NAME_INPUT;
        popupPrompt = promptForAction(action);
        popupInput.setValue("");
        this.setFocused(popupInput);
        popupInput.setFocused(true);
    }

    private String promptForAction(int action) {
        switch (action) {
            case 1: return "New group name:";
            case 2: return "New list name:";
            case 4: return "Rename list:";
            case 5: return "Rename group:";
            case 6: return "Preset name:";
            case 10: return "New group name:";
            case 11: return "New list name:";
            case 12: return "New list name:";
            default: return "Name:";
        }
    }

    private void confirmNameInput() {
        String name = popupInput.getValue().trim();
        if (name.isEmpty()) {
            popup = Popup.NONE;
            return;
        }
        GradientConfig config = GradientConfig.get();

        switch (pendingNameAction) {
            case 1: { // add group
                GradientConfig.GroupEntry group = new GradientConfig.GroupEntry(name);
                config.getGroups().add(group);
                selGroupIndex = config.getGroups().size() - 1;
                selListIndex = -1;
                viewUnassigned = false;
                break;
            }
            case 2: { // add list to selected (or default) group
                GradientConfig.GroupEntry group = getSelectedGroup() != null ? getSelectedGroup() : defaultGroup();
                selGroupIndex = config.getGroups().indexOf(group);
                GradientConfig.ListEntry list = new GradientConfig.ListEntry(name);
                group.getLists().add(list);
                selListIndex = group.getLists().size() - 1;
                viewUnassigned = false;
                break;
            }
            case 4: { // rename list
                if (popupTargetList != null) {
                    popupTargetList.setName(name);
                }
                break;
            }
            case 5: { // rename group
                if (popupTargetGroup != null) {
                    popupTargetGroup.setName(name);
                }
                break;
            }
            case 6: { // export preset
                String fileName = ConfigManager.exportPreset(name);
                if (fileName != null) {
                    statusMsg = "Exported: " + fileName;
                    statusColor = 0x55FF55;
                } else {
                    statusMsg = "Export failed";
                    statusColor = 0xFF5555;
                }
                popup = Popup.NONE;
                return;
            }
            case 10: { // "New Group + List" step 1: create group, then prompt list name
                GradientConfig.GroupEntry group = new GradientConfig.GroupEntry(name);
                config.getGroups().add(group);
                selGroupIndex = config.getGroups().size() - 1;
                selListIndex = -1;
                viewUnassigned = false;
                pendingNameAction = 11;
                popupPrompt = promptForAction(11);
                popupInput.setValue("");
                this.setFocused(popupInput);
                popupInput.setFocused(true);
                return;
            }
            case 11: { // "New Group + List" step 2: create list, then add item
                GradientConfig.GroupEntry group = getSelectedGroup() != null ? getSelectedGroup() : defaultGroup();
                selGroupIndex = config.getGroups().indexOf(group);
                GradientConfig.ListEntry list = new GradientConfig.ListEntry(name);
                group.getLists().add(list);
                selListIndex = group.getLists().size() - 1;
                viewUnassigned = false;
                finishAddToList(list);
                return;
            }
            case 12: { // "New List" (add flow): create list in default group, then add item
                GradientConfig.GroupEntry group = defaultGroup();
                selGroupIndex = config.getGroups().indexOf(group);
                GradientConfig.ListEntry list = new GradientConfig.ListEntry(name);
                group.getLists().add(list);
                selListIndex = group.getLists().size() - 1;
                viewUnassigned = false;
                finishAddToList(list);
                return;
            }
        }
        ConfigManager.save();
        statusMsg = "Done";
        statusColor = 0x55FF55;
        popup = Popup.NONE;
    }

    // ================================================================
    // POPUP GEOMETRY
    // ================================================================
    private int popupW() { return 300; }
    private int popupH() {
        switch (popup) {
            case ADD_CHOICE: return 120;
            case NAME_INPUT: return 96;
            case CONFIRM_DELETE: return 100;
            case PICK_LIST:
            case PICK_GROUP:
            case PICK_PRESET: return Math.min(260, height - 40);
            default: return 100;
        }
    }
    private int popupX() { return (width - popupW()) / 2; }
    private int popupY() { return (height - popupH()) / 2; }
    private int popupBottom() { return popupY() + popupH() - 24; }
    private int popupListTop() { return popupY() + 30; }

    // ================================================================
    // RENDER
    // ================================================================
    @Override
    public void renderBackground(GuiGraphics g) {
        // no-op: BackgroundPatterns.render draws the block-pattern background
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (itemIdInput == null || toolGradientsBtn == null) return;
        buildRows();

        if (popup != Popup.NAME_INPUT) {
            popupInput.setFocused(false);
        }

        // background
        BackgroundPatterns.render(g, width, height, currentBackgroundPattern);

        // header
        g.fill(0, 0, width, headerH, 0xE01A1A22);
        g.drawString(font, "GRADIENT CONFIG", margin, 6, 0x55FFFF, true);
        g.drawString(font, "Group > List > Items", margin + 110, 8, 0x666666);

        // panels (translucent so the block pattern shows through)
        g.fill(leftX, contentTop, leftX + leftW, contentBottom, 0x601A1A22);
        g.fill(centerX, contentTop, centerX + centerW, contentBottom, 0x6016161C);
        g.fill(rightX, contentTop, rightX + rightW, contentBottom, 0x601A1A22);

        renderLeftPanel(g, mx, my);
        renderCenterPanel(g, mx, my);
        renderFormPanel(g, mx, my);

        // popup
        if (popup != Popup.NONE) {
            renderPopup(g, mx, my);
        }

        // status
        if (!statusMsg.isEmpty()) {
            g.fill(rightX, height - 24, rightX + rightW, height - 8, 0xFF000000);
            g.drawString(font, statusMsg, rightX + 2, height - 21, statusColor);
        }

        super.render(g, mx, my, pt);

        // suggestions drawn LAST so the dropdown always fully overlays the GUI
        renderSuggestions(g, mx, my);
    }

    private void renderLeftPanel(GuiGraphics g, int mx, int my) {
        if (showPatternTab) {
            List<BackgroundPatterns.Pattern> pats = BackgroundPatterns.getAll();
            for (int[] r : leftRows) {
                int idx = r[5];
                if (idx < 0 || idx >= pats.size()) continue;
                BackgroundPatterns.Pattern p = pats.get(idx);
                boolean sel = p.id.equals(currentBackgroundPattern);
                if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x5055AAFF);
                else if (sel) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x403388CC);
                g.drawString(font, p.displayName, r[0] + 4, r[1] + 3, sel ? 0x55FFFF : 0xDDDDDD);
                int barW = 40;
                int bx = r[0] + r[2] - barW - 4;
                int by = r[1] + 6;
                int n = p.blocks.size();
                if (n == 0 && p.gradient != null) {
                    g.fillGradient(bx, by, bx + barW, by + 10, p.gradient[0], p.gradient[1]);
                } else if (n == 0) {
                    g.fill(bx, by, bx + barW, by + 10, 0xFF000000);
                } else {
                    int col = Math.max(1, barW / n);
                    for (int b = 0; b < n; b++) {
                        TextureAtlasSprite sp = BackgroundPatterns.sprite(p.blocks.get(b));
                        if (sp == null) continue;
                        int cw = (b == n - 1) ? barW - col * (n - 1) : col;
                        g.blit(bx + b * col, by, 0, cw, 10, sp);
                    }
                }
                g.drawString(font, "click to select", r[0] + 4, r[1] + 15, 0x666666);
            }
            return;
        }
        if (showBlacklistTab) {
            g.drawString(font, "Blacklist mode", leftX + 4, contentTop + 60, 0xAAAAAA);
            g.drawString(font, "Click Blacklist rows in", leftX + 4, contentTop + 74, 0x666666);
            g.drawString(font, "the center panel to", leftX + 4, contentTop + 86, 0x666666);
            g.drawString(font, "select / remove them.", leftX + 4, contentTop + 98, 0x666666);
            return;
        }

        int y = leftListTop();
        List<GradientConfig.GroupEntry> groups = GradientConfig.get().getGroups();
        for (int[] r : leftRows) {
            int type = r[4];
            if (type == 0) {
                GradientConfig.GroupEntry gr = groups.get(r[5]);
                boolean sel = r[5] == selGroupIndex;
                if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x505555FF);
                else if (sel) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x40333388);
                String label = (sel ? "▼ " : "▶ ") + gr.getName();
                g.drawString(font, label, r[0] + 2, r[1] + 4, sel ? 0x55FFFF : 0xDDDDDD);
            } else if (type == 1) {
                GradientConfig.ListEntry ls = groups.get(r[5]).getLists().get(r[6]);
                boolean sel = r[5] == selGroupIndex && r[6] == selListIndex;
                if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x5055FF55);
                else if (sel) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x40338833);
                g.drawString(font, "· " + ls.getName() + " (" + ls.getItems().size() + ")", r[0] + 2, r[1] + 3, sel ? 0x55FF55 : 0xBBBBBB);
            } else if (type == 2) {
                boolean sel = viewUnassigned;
                if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x50FFAA00);
                else if (sel) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x40AA5500);
                g.drawString(font, "[Unassigned] (" + GradientConfig.get().getUnassigned().size() + ")", r[0] + 2, r[1] + 4, sel ? 0xFFAA00 : 0xBBBBBB);
            }
        }

        g.fill(leftX + 2, y, leftX + leftW - 2, y + 1, 0xFF333333);
        g.drawString(font, scrollLeft > 0 ? "▲ scroll ▲" : "", leftX + 4, contentBottom - 10, 0x444444);
    }

    private void renderCenterPanel(GuiGraphics g, int mx, int my) {
        if (showBlacklistTab) {
            g.drawString(font, "BLACKLIST (" + GradientConfig.get().getBlacklistedItems().size() + ")", centerX + 2, contentTop + 2, 0xFF5555, true);
            List<String> bl = new ArrayList<>(GradientConfig.get().getBlacklistedItems());
            for (int i = 0; i < itemRows.size(); i++) {
                int[] r = itemRows.get(i);
                boolean sel = i == selBlacklistIndex;
                if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x50FF5555);
                else if (sel) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x40883333);
                g.drawString(font, bl.get(i), r[0] + 2, r[1] + 5, sel ? 0xFF5555 : 0xDDDDDD);
            }
            if (bl.isEmpty()) g.drawString(font, "(empty)", centerX + 2, centerListTop() + 4, 0x666666);
            return;
        }

        // header
        String title;
        if (viewUnassigned) {
            title = "UNASSIGNED (" + GradientConfig.get().getUnassigned().size() + ")";
        } else {
            GradientConfig.ListEntry list = getSelectedList();
            GradientConfig.GroupEntry group = getSelectedGroup();
            if (list != null && group != null) {
                title = group.getName() + " > " + list.getName() + " (" + list.getItems().size() + ")";
            } else {
                title = "Select a list";
            }
        }
        g.drawString(font, title, centerX + 2, contentTop + 2, 0x55FFFF, true);

        List<GradientConfig.ItemGradientEntry> items = getViewItems();
        for (int i = 0; i < itemRows.size(); i++) {
            int[] r = itemRows.get(i);
            if (i >= items.size()) break;
            GradientConfig.ItemGradientEntry e = items.get(i);
            boolean sel = i == selItemIndex;
            if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x505555FF);
            else if (sel) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x40333388);
            g.drawString(font, e.getItemId(), r[0] + 2, r[1] + 5, sel ? 0x55FFFF : 0xFFFFFF);
            String info = colorSummary(e.getColors()) + " | " + e.getMode() + "/" + e.getDirection() + " " + e.getSpeed() + "x" + (e.isBold() ? " B" : "");
            g.drawString(font, info, r[0] + 2, r[1] + 14, 0x888888);
        }
        if (items.isEmpty() && (viewUnassigned || getSelectedList() != null)) {
            g.drawString(font, "(empty)", centerX + 2, centerListTop() + 4, 0x666666);
        }
        if (getSelectedList() == null && !viewUnassigned) {
            g.drawString(font, "Click a list on the left,", centerX + 2, centerListTop() + 40, 0x666666);
            g.drawString(font, "or add an item with the", centerX + 2, centerListTop() + 52, 0x666666);
            g.drawString(font, "form on the right.", centerX + 2, centerListTop() + 64, 0x666666);
        }
    }

    private void renderFormPanel(GuiGraphics g, int mx, int my) {
        // labels
        g.drawString(font, "Item ID", rightX, contentTop - 9, 0xAAAAAA);
        g.drawString(font, "Custom Name", rightX, contentTop + 21, 0xAAAAAA);
        g.drawString(font, "Colors (comma-separated)", rightX, contentTop + 51, 0xAAAAAA);
        g.drawString(font, "Direction", rightX, contentTop + 81, 0xAAAAAA);
        g.drawString(font, "Mode", rightX, contentTop + 111, 0xAAAAAA);
        g.drawString(font, "Speed", rightX, contentTop + 141, 0xAAAAAA);

        itemIdInput.render(g, mx, my, pt());
        nameInput.render(g, mx, my, pt());
        colorsInput.render(g, mx, my, pt());
        directionInput.render(g, mx, my, pt());
        modeInput.render(g, mx, my, pt());
        speedInput.render(g, mx, my, pt());

        // position buttons dynamically
        int fy = contentTop;
        boldBtn.setPosition(rightX, fy + 168);
        addBtn.setPosition(rightX, fy + 190);
        if (editing) {
            addBtn.setMessage(Component.literal("UPDATE"));
            cancelEditBtn.setMessage(Component.literal("Cancel Edit"));
            cancelEditBtn.setPosition(rightX, fy + 212);
            cancelEditBtn.visible = true;
        } else {
            addBtn.setMessage(Component.literal("ADD"));
            cancelEditBtn.visible = false;
        }
        addBlacklistBtn.setPosition(rightX, fy + 232);
        saveBtn.setPosition(rightX, fy + 254);
        doneBtn.setPosition(rightX, fy + 276);
    }

    private float pt() { return 0f; }

    private void renderSuggestions(GuiGraphics g, int mx, int my) {
        if (popup != Popup.NONE) return;
        if (currentSuggestions.isEmpty()) return;
        int sx = rightX;
        int sy;
        switch (activeField) {
            case 0: sy = contentTop + 16 + 2; break;
            case 2: sy = contentTop + 60 + 16 + 2; break;
            case 3: sy = contentTop + 90 + 16 + 2; break;
            case 4: sy = contentTop + 120 + 16 + 2; break;
            default: return;
        }
        int sw = rightW;
        int shown = Math.min(currentSuggestions.size(), 8);
        if (shown <= 0) return;
        int sh = shown * 14 + 4;
        // solid, opaque dropdown so it never blends with the fields underneath
        g.fill(sx, sy, sx + sw, sy + sh, 0xFF20202E);
        g.fill(sx, sy, sx + sw, sy + 1, 0xFF55FFFF);
        g.fill(sx, sy + sh - 1, sx + sw, sy + sh, 0xFF55FFFF);
        g.fill(sx, sy, sx + 1, sy + sh, 0xFF55FFFF);
        g.fill(sx + sw - 1, sy, sx + sw, sy + sh, 0xFF55FFFF);
        for (int i = 0; i < shown; i++) {
            int y = sy + 2 + i * 14;
            boolean selected = (i == sugSelectIndex);
            boolean hov = mx >= sx && mx <= sx + sw && my >= y && my <= y + 12;
            if (selected || hov) g.fill(sx + 1, y, sx + sw - 1, y + 12, 0xFF3344AA);
            g.drawString(font, Component.literal(currentSuggestions.get(i)), sx + 6, y + 2, selected || hov ? 0xFFFFFF : 0xDDDDDD, true);
        }
    }

    private void renderPopup(GuiGraphics g, int mx, int my) {
        int px = popupX(), py = popupY(), pw = popupW(), ph = popupH();
        g.fill(0, 0, width, height, 0x80000000);
        g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF555555);
        g.fill(px, py, px + pw, py + ph, 0xFF181820);
        g.drawString(font, popupTitle, px + 12, py + 8, 0x55FFFF, true);

        switch (popup) {
            case ADD_CHOICE: {
                drawPopupButton(g, px + 30, py + 30, pw - 60, 18, "New List...", isInRect(mx, my, px + 30, py + 30, pw - 60, 18));
                drawPopupButton(g, px + 30, py + 52, pw - 60, 18, "New Group + List...", isInRect(mx, my, px + 30, py + 52, pw - 60, 18));
                drawPopupButton(g, px + 30, py + 74, pw - 60, 18, "Skip (Unassigned)", isInRect(mx, my, px + 30, py + 74, pw - 60, 18));
                break;
            }
            case NAME_INPUT: {
                popupInput.setPosition(px + 30, py + 34);
                popupInput.setWidth(pw - 60);
                popupInput.render(g, mx, my, pt());
                drawPopupButton(g, px + 30, py + 60, (pw - 60) / 2 - 2, 18, "OK", isInRect(mx, my, px + 30, py + 60, (pw - 60) / 2 - 2, 18));
                drawPopupButton(g, px + 30 + (pw - 60) / 2 + 2, py + 60, (pw - 60) / 2 - 2, 18, "Cancel", isInRect(mx, my, px + 30 + (pw - 60) / 2 + 2, py + 60, (pw - 60) / 2 - 2, 18));
                g.drawString(font, popupPrompt, px + 12, py + 20, 0xAAAAAA);
                break;
            }
            case CONFIRM_DELETE: {
                drawPopupButton(g, px + 30, py + 30, pw - 60, 18, "Delete items entirely", isInRect(mx, my, px + 30, py + 30, pw - 60, 18));
                drawPopupButton(g, px + 30, py + 52, pw - 60, 18, "Move into another...", isInRect(mx, my, px + 30, py + 52, pw - 60, 18));
                drawPopupButton(g, px + 30, py + 74, pw - 60, 18, "Cancel", isInRect(mx, my, px + 30, py + 74, pw - 60, 18));
                break;
            }
            case PICK_LIST:
            case PICK_GROUP: {
                for (PopupRow r : popupRows) {
                    if (isInRect(mx, my, r.x, r.y, r.w, r.h)) {
                        g.fill(r.x, r.y, r.x + r.w, r.y + r.h, 0x505555FF);
                    }
                    g.drawString(font, rowLabel(r), r.x + 4, r.y + 3, 0xDDDDDD);
                }
                drawPopupButton(g, px + 30, py + ph - 22, pw - 60, 18, "Cancel", isInRect(mx, my, px + 30, py + ph - 22, pw - 60, 18));
                break;
            }
            case PICK_PRESET: {
                for (int i = 0; i < presetRows.size(); i++) {
                    int[] r = presetRows.get(i);
                    if (isInRect(mx, my, r)) g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0x505555FF);
                    g.drawString(font, popupPresets.get(i), r[0] + 4, r[1] + 3, 0xDDDDDD);
                }
                if (popupPresets.isEmpty()) {
                    g.drawString(font, "(no presets exported yet)", px + 12, py + 40, 0x666666);
                }
                drawPopupButton(g, px + 30, py + ph - 22, pw - 60, 18, "Cancel", isInRect(mx, my, px + 30, py + ph - 22, pw - 60, 18));
                break;
            }
            default: break;
        }
    }

    private String rowLabel(PopupRow r) {
        if (r.group == null) {
            return "[Unassigned]";
        }
        if (r.list == null) {
            return r.group.getName();
        }
        return r.group.getName() + " > " + r.list.getName();
    }

    private void drawPopupButton(GuiGraphics g, int x, int y, int w, int h, String label, boolean hover) {
        g.fill(x, y, x + w, y + h, hover ? 0xFF333377 : 0xFF222233);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, hover ? 0xFF444488 : 0xFF2A2A3A);
        g.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFF);
    }

    private boolean isInRect(double mx, double my, int[] r) {
        return mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3];
    }
    private boolean isInRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // ================================================================
    // MOUSE
    // ================================================================
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        buildRows();

        if (popup != Popup.NONE) {
            return handlePopupClick(mx, my, btn);
        }

        if (btn == 0) {
            // suggestions
            if (!currentSuggestions.isEmpty()) {
                int sx = rightX;
                int sy;
                switch (activeField) {
                    case 0: sy = contentTop + 16 + 2; break;
                    case 2: sy = contentTop + 60 + 16 + 2; break;
                    case 3: sy = contentTop + 90 + 16 + 2; break;
                    case 4: sy = contentTop + 120 + 16 + 2; break;
                    default: sy = -1; break;
                }
                if (sy >= 0) {
                    int shown = Math.min(currentSuggestions.size(), 8);
                    for (int i = 0; i < shown; i++) {
                        int y = sy + 2 + i * 14;
                        if (mx >= sx && mx <= sx + rightW && my >= y && my <= y + 12) {
                            applySuggestion(currentSuggestions.get(i));
                            return true;
                        }
                    }
                }
            }

            // left panel rows
            for (int[] r : leftRows) {
                if (isInRect(mx, my, r)) {
                    int type = r[4];
                    if (type == 0) {
                        if (selGroupIndex == r[5]) {
                            selGroupIndex = -1;
                            selListIndex = -1;
                            viewUnassigned = false;
                        } else {
                            selGroupIndex = r[5];
                            selListIndex = -1;
                            viewUnassigned = false;
                        }
                        selItemIndex = -1;
                    } else if (type == 1) {
                        selGroupIndex = r[5];
                        selListIndex = r[6];
                        viewUnassigned = false;
                        selItemIndex = -1;
                    } else if (type == 2) {
                        viewUnassigned = true;
                        selListIndex = -1;
                        selGroupIndex = -1;
                        selItemIndex = -1;
                    } else if (type == 3) {
                        List<BackgroundPatterns.Pattern> pats = BackgroundPatterns.getAll();
                        if (r[5] >= 0 && r[5] < pats.size()) {
                            currentBackgroundPattern = pats.get(r[5]).id;
                            statusMsg = "Pattern: " + pats.get(r[5]).displayName;
                            statusColor = 0x55FF55;
                        }
                    }
                    return true;
                }
            }

            // center panel rows
            if (showBlacklistTab) {
                for (int i = 0; i < itemRows.size(); i++) {
                    if (isInRect(mx, my, itemRows.get(i))) {
                        selBlacklistIndex = i;
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < itemRows.size(); i++) {
                    if (isInRect(mx, my, itemRows.get(i))) {
                        selItemIndex = i;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private boolean handlePopupClick(double mx, double my, int btn) {
        int px = popupX(), py = popupY(), pw = popupW(), ph = popupH();
        if (btn != 0) return true;

        switch (popup) {
            case ADD_CHOICE:
                if (isInRect(mx, my, px + 30, py + 30, pw - 60, 18)) {
                    startNamePrompt(12); // new list in default group, then add item
                    return true;
                }
                if (isInRect(mx, my, px + 30, py + 52, pw - 60, 18)) {
                    startNamePrompt(10); // new group + list, then add item
                    return true;
                }
                if (isInRect(mx, my, px + 30, py + 74, pw - 60, 18)) {
                    finishAddToUnassigned();
                    return true;
                }
                return true;

            case NAME_INPUT:
                if (isInRect(mx, my, px + 30, py + 34, pw - 60, 16)) {
                    this.setFocused(popupInput);
                    popupInput.setFocused(true);
                    popupInput.mouseClicked(mx, my, btn);
                    return true;
                }
                if (isInRect(mx, my, px + 30, py + 60, (pw - 60) / 2 - 2, 18)) {
                    confirmNameInput();
                    return true;
                }
                if (isInRect(mx, my, px + 30 + (pw - 60) / 2 + 2, py + 60, (pw - 60) / 2 - 2, 18)) {
                    popup = Popup.NONE;
                    return true;
                }
                return true;

            case CONFIRM_DELETE:
                if (isInRect(mx, my, px + 30, py + 30, pw - 60, 18)) {
                    doDeleteConfirmed();
                    return true;
                }
                if (isInRect(mx, my, px + 30, py + 52, pw - 60, 18)) {
                    if (popupTargetIsGroup) {
                        popup = Popup.PICK_GROUP;
                        popupTitle = "Move group's lists into:";
                        popupScroll = 0;
                    } else {
                        popup = Popup.PICK_LIST;
                        popupTitle = "Move items into:";
                        popupScroll = 0;
                    }
                    return true;
                }
                if (isInRect(mx, my, px + 30, py + 74, pw - 60, 18)) {
                    popup = Popup.NONE;
                    return true;
                }
                return true;

            case PICK_LIST:
                for (PopupRow r : popupRows) {
                    if (isInRect(mx, my, r.x, r.y, r.w, r.h)) {
                        if (r.group == null && r.list == null) {
                            // unassigned target
                            if (popupTargetItem != null) {
                                doMoveItem(null, true);
                            } else {
                                popup = Popup.NONE;
                            }
                        } else if (r.list != null) {
                            if (popupTargetItem != null) {
                                doMoveItem(r.list, false);
                            } else if (popupTargetList != null) {
                                // moving a list's items (delete-move flow)
                                GradientConfig.GroupEntry srcGroup = getSelectedGroup();
                                if (srcGroup != null) {
                                    srcGroup.getLists().remove(popupTargetList);
                                    r.list.getItems().addAll(popupTargetList.getItems());
                                    ConfigManager.save();
                                    selListIndex = -1;
                                    popup = Popup.NONE;
                                    statusMsg = "Moved items into " + r.list.getName();
                                    statusColor = 0x55FF55;
                                }
                            }
                        }
                        return true;
                    }
                }
                if (isInRect(mx, my, px + 30, py + ph - 22, pw - 60, 18)) {
                    popup = Popup.NONE;
                }
                return true;

            case PICK_GROUP:
                for (PopupRow r : popupRows) {
                    if (isInRect(mx, my, r.x, r.y, r.w, r.h)) {
                        if (r.group != null) {
                            GradientConfig.GroupEntry srcGroup = getSelectedGroup();
                            if (srcGroup != null && selListIndex >= 0 && selListIndex < srcGroup.getLists().size()) {
                                GradientConfig.ListEntry list = srcGroup.getLists().get(selListIndex);
                                srcGroup.getLists().remove(list);
                                r.group.getLists().add(list);
                                selGroupIndex = GradientConfig.get().getGroups().indexOf(r.group);
                                selListIndex = r.group.getLists().size() - 1;
                                ConfigManager.save();
                                statusMsg = "Moved list to " + r.group.getName();
                                statusColor = 0x55FF55;
                            }
                        }
                        popup = Popup.NONE;
                        return true;
                    }
                }
                if (isInRect(mx, my, px + 30, py + ph - 22, pw - 60, 18)) {
                    popup = Popup.NONE;
                }
                return true;

            case PICK_PRESET:
                for (int i = 0; i < presetRows.size(); i++) {
                    if (isInRect(mx, my, presetRows.get(i))) {
                        doImportPreset(popupPresets.get(i));
                        return true;
                    }
                }
                if (isInRect(mx, my, px + 30, py + ph - 22, pw - 60, 18)) {
                    popup = Popup.NONE;
                }
                return true;

            default:
                return true;
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (popup == Popup.PICK_LIST || popup == Popup.PICK_GROUP || popup == Popup.PICK_PRESET) {
            popupScroll = Math.max(0, popupScroll - (int) (delta * 12));
            return true;
        }
        if (popup != Popup.NONE) return true;
        if (mx >= leftX && mx <= leftX + leftW) {
            scrollLeft = Math.max(0, scrollLeft - (int) (delta * 12));
            return true;
        }
        if (mx >= centerX && mx <= centerX + centerW) {
            scrollItems = Math.max(0, scrollItems - (int) (delta * 12));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    // ================================================================
    // KEYBOARD
    // ================================================================
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (popup == Popup.NAME_INPUT) {
            if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                confirmNameInput();
                return true;
            }
            if (keyCode == InputConstants.KEY_ESCAPE) {
                popup = Popup.NONE;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (popup != Popup.NONE) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                popup = Popup.NONE;
                return true;
            }
            return true;
        }
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ================================================================
    // SUGGESTIONS
    // ================================================================
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
