package com.scrowl.gradienttext.gui;

import com.scrowl.gradienttext.gradient.WandGradient;
import com.scrowl.gradienttext.item.DepthGradientWandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DepthWandPickerScreen extends Screen {
    private final List<WandGradient> gradients = WandGradient.getAll();
    private int scroll = 0;
    private final int rowH = 18;

    public DepthWandPickerScreen() {
        super(Component.literal("Depth Gradient Wand"));
    }

    private ItemStack held() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(0, 0, width, height, 0xE0101010, 0xE0101010);
        g.drawString(font, "SELECT GRADIENT", 8, 6, 0x55FFFF, true);
        g.drawString(font, "then depth + whitelist; right-click = paint where you aim", 8, 18, 0x666666);

        int listTop = 30;
        for (int i = 0; i < gradients.size(); i++) {
            int y = listTop + (i - scroll) * rowH;
            if (y < listTop - rowH || y > height - 72) continue;
            WandGradient wg = gradients.get(i);
            boolean sel = wg.id.equals(DepthGradientWandItem.getGradientId(held()));
            boolean hov = mx >= 8 && mx < width - 8 && my >= y && my < y + rowH - 2;
            if (hov) g.fill(8, y, width - 8, y + rowH - 2, 0x5055AAFF);
            else if (sel) g.fill(8, y, width - 8, y + rowH - 2, 0x403388CC);
            int barW = 26;
            g.fillGradient(10, y + 4, 10 + barW, y + rowH - 6,
                    0xFF000000 | wg.colors[0], 0xFF000000 | wg.colors[wg.colors.length - 1]);
            g.drawString(font, wg.displayName, 10 + barW + 6, y + 4, sel ? 0x55FFFF : 0xDDDDDD);
        }

        int fy = height - 52;
        drawBtn(g, width / 2 + 40, fy - 40, 40, 12, "-");
        drawBtn(g, width / 2 + 88, fy - 40, 40, 12, "+");
        g.drawString(font, "Depth: " + DepthGradientWandItem.getDepth(held()),
                width / 2 + 132, fy - 38, 0xCCCCCC);

        drawBtn(g, width / 2 + 40, fy - 20, 40, 12, "-");
        drawBtn(g, width / 2 + 88, fy - 20, 40, 12, "+");
        g.drawString(font, "Radius: " + DepthGradientWandItem.getRadius(held()),
                width / 2 + 132, fy - 18, 0xCCCCCC);

        drawBtn(g, 8, fy, 120, 14, "Whitelist (" + DepthGradientWandItem.getWhitelist(held()).size() + ")");
        g.drawString(font, "Scroll to browse", 8, fy - 60, 0x666666);
        super.render(g, mx, my, pt);
    }

    private void drawBtn(GuiGraphics g, int x, int y, int w, int h, String label) {
        g.fill(x, y, x + w, y + h, 0xFF222233);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF2A2A3A);
        g.drawCenteredString(font, label, x + w / 2, y + 1, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        ItemStack held = held();
        if (held.isEmpty()) return super.mouseClicked(mx, my, button);

        int listTop = 30;
        for (int i = 0; i < gradients.size(); i++) {
            int y = listTop + (i - scroll) * rowH;
            if (my >= y && my < y + rowH - 2 && mx >= 8 && mx < width - 8) {
                DepthGradientWandItem.setGradientId(held, gradients.get(i).id);
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            Component.literal("Gradient: ").append(gradients.get(i).displayName)
                                    .withStyle(ChatFormatting.AQUA), true);
                }
                onClose();
                return true;
            }
        }

        int fy = height - 52;
        if (mx >= width / 2 + 40 && mx < width / 2 + 80 && my >= fy - 40 && my < fy - 28) {
            DepthGradientWandItem.setDepth(held, DepthGradientWandItem.getDepth(held) - 1);
            return true;
        }
        if (mx >= width / 2 + 88 && mx < width / 2 + 128 && my >= fy - 40 && my < fy - 28) {
            DepthGradientWandItem.setDepth(held, DepthGradientWandItem.getDepth(held) + 1);
            return true;
        }
        if (mx >= width / 2 + 40 && mx < width / 2 + 80 && my >= fy - 20 && my < fy - 8) {
            DepthGradientWandItem.setRadius(held, DepthGradientWandItem.getRadius(held) - 1);
            return true;
        }
        if (mx >= width / 2 + 88 && mx < width / 2 + 128 && my >= fy - 20 && my < fy - 8) {
            DepthGradientWandItem.setRadius(held, DepthGradientWandItem.getRadius(held) + 1);
            return true;
        }
        if (mx >= 8 && mx < 128 && my >= fy && my < fy + 14) {
            if (held.getItem() instanceof DepthGradientWandItem) {
                Minecraft.getInstance().setScreen(new WhitelistScreen());
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scroll = Math.max(0, Math.min(gradients.size() - 1, scroll - (int) delta));
        return true;
    }
}