package com.scrowl.gradienttext.gui;

import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientEngine;
import com.scrowl.gradienttext.gradient.GradientMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GradientScreen extends Screen {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private final GradientMenu menu;
    private int leftPos;
    private int topPos;

    private Button directionButton;
    private Button modeButton;
    private Button boldButton;
    private Button speedButton;
    private Button applyButton;
    private Button removeButton;
    private Button presetButton;

    private int currentPreset = 0;
    private final List<int[]> presetColors = new ArrayList<>();

    public GradientScreen(GradientMenu menu, Inventory playerInventory, Component title) {
        super(title);
        this.menu = menu;

        presetColors.add(new int[]{0xFF0000, 0xFFFF00});
        presetColors.add(new int[]{0x0000FF, 0x00FFFF});
        presetColors.add(new int[]{0xFF00FF, 0x00FF00});
        presetColors.add(new int[]{0xFF0000, 0xFFAA00, 0xFFFF00});
        presetColors.add(new int[]{0xFF0000, 0xFF00FF, 0x0000FF, 0x00FFFF});
        presetColors.add(new int[]{0xFF5500, 0xFF55FF, 0x55FFFF, 0x55FF55});
        presetColors.add(new int[]{0xFF0000, 0xFF5500, 0xFFFF00, 0x55FF55, 0x5555FF, 0xAA00FF});
        presetColors.add(new int[]{0xFF0000, 0xFFAA00, 0xFFFF00, 0x55FF55, 0x00FFFF, 0x5555FF});
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        int btnX = leftPos + 8;
        int btnW = 75;
        int btnH = 20;

        directionButton = addRenderableWidget(new Button.Builder(
                Component.literal("Horizontal"),
                button -> {
                    boolean newVertical = menu.getCurrentDirection() == GradientDirection.HORIZONTAL;
                    menu.setCurrentDirection(newVertical ? GradientDirection.VERTICAL : GradientDirection.HORIZONTAL);
                    button.setMessage(Component.literal(newVertical ? "Vertical" : "Horizontal"));
                }
        ).pos(btnX, topPos + 20).size(btnW, btnH).build());

        modeButton = addRenderableWidget(new Button.Builder(
                Component.literal("Static"),
                button -> {
                    GradientMode current = menu.getCurrentMode();
                    GradientMode next;
                    if (current == GradientMode.STATIC) next = GradientMode.DYNAMIC;
                    else if (current == GradientMode.DYNAMIC) next = GradientMode.SMOOTH;
                    else next = GradientMode.STATIC;
                    menu.setCurrentMode(next);
                    button.setMessage(Component.literal(next.getDisplayName().split(" ")[0]));
                }
        ).pos(btnX, topPos + 40).size(btnW, btnH).build());

        boldButton = addRenderableWidget(new Button.Builder(
                Component.literal("Bold: OFF"),
                button -> {
                    boolean newBold = !menu.isCurrentBold();
                    menu.setCurrentBold(newBold);
                    button.setMessage(Component.literal("Bold: " + (newBold ? "ON" : "OFF")));
                }
        ).pos(btnX, topPos + 60).size(btnW, btnH).build());

        speedButton = addRenderableWidget(new Button.Builder(
                Component.literal("Speed: 1.0x"),
                button -> {
                    float speed = menu.getCurrentSpeed();
                    if (speed < 1.0f) speed = 1.0f;
                    else if (speed < 2.0f) speed = 2.0f;
                    else if (speed < 3.0f) speed = 3.0f;
                    else if (speed < 5.0f) speed = 5.0f;
                    else speed = 0.5f;
                    menu.setCurrentSpeed(speed);
                    button.setMessage(Component.literal("Speed: " + speed + "x"));
                }
        ).pos(btnX, topPos + 80).size(btnW, btnH).build());

        applyButton = addRenderableWidget(new Button.Builder(
                Component.literal("Apply"),
                button -> menu.applyToHeldItemClient()
        ).pos(btnX, topPos + 100).size(btnW, btnH).build());

        removeButton = addRenderableWidget(new Button.Builder(
                Component.literal("Remove"),
                button -> {
                    if (minecraft != null && minecraft.player != null) {
                        ItemStack heldItem = minecraft.player.getMainHandItem();
                        if (!heldItem.isEmpty()) {
                            GradientData.removeFromItemStack(heldItem);
                        }
                    }
                }
        ).pos(btnX, topPos + 120).size(btnW, btnH).build());

        presetButton = addRenderableWidget(new Button.Builder(
                Component.literal("Preset"),
                button -> {
                    currentPreset = (currentPreset + 1) % presetColors.size();
                    menu.setCurrentColors(presetColors.get(currentPreset));
                }
        ).pos(btnX, topPos + 140).size(btnW, btnH).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + GUI_WIDTH, this.topPos + GUI_HEIGHT, 0xC0100010);

        renderGradientPreview(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderGradientPreview(GuiGraphics guiGraphics) {
        int previewX = leftPos + 90;
        int previewY = topPos + 20;
        int previewW = 75;
        int previewH = 20;

        guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xFF000000);

        int[] colors = menu.getCurrentColors();
        if (colors != null && colors.length > 0) {
            Component previewText = GradientEngine.applyGradient(
                    "Preview",
                    colors,
                    menu.getCurrentDirection(),
                    menu.isCurrentBold()
            );
            guiGraphics.drawCenteredString(this.font, previewText, previewX + previewW / 2, previewY + 6, 0xFFFFFF);
        }

        Component label = Component.literal("Preview:");
        guiGraphics.fill(previewX, previewY - 12, previewX + 50, previewY - 2, 0xFF000000);
        guiGraphics.drawString(this.font, label, previewX, previewY - 10, 0xFFFFFF);

        String currentMode = menu.getCurrentMode().getDisplayName();
        guiGraphics.fill(previewX, previewY + previewH + 3, previewX + 80, previewY + previewH + 15, 0xFF000000);
        guiGraphics.drawString(this.font, Component.literal("Mode: " + currentMode), previewX, previewY + previewH + 5, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
