package com.scrowl.gradienttext.gui;

import com.scrowl.gradienttext.item.DepthGradientWandItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class WhitelistScreen extends Screen {
    private final List<String> allBlocks = new ArrayList<>();
    private EditBox search;
    private int scroll = 0;
    private final int rowH = 12;

    public WhitelistScreen() {
        super(Component.literal("Whitelist"));
        for (Block b : ForgeRegistries.BLOCKS) {
            if (b == Blocks.AIR || b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR) continue;
            if (b == Blocks.BEDROCK || b == Blocks.BARRIER || b == Blocks.WATER || b == Blocks.LAVA) continue;
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(b);
            if (key == null) continue;
            allBlocks.add(key.toString());
        }
        allBlocks.sort(String::compareTo);
    }

    private ItemStack held() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        search = new EditBox(font, 8, 6, Math.min(200, width - 16), 12, Component.literal("search"));
        search.setMaxLength(64);
        search.setFocused(true);
        addRenderableWidget(search);
    }

    private List<String> filtered() {
        String q = search.getValue().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return allBlocks;
        return allBlocks.stream().filter(s -> s.contains(q)).collect(Collectors.toList());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(0, 0, width, height, 0xE0101010, 0xE0101010);
        g.drawString(font, "Whitelist: click blocks to toggle. Search to filter.", 8, 20, 0x666666);
        search.render(g, mx, my, pt);

        List<String> list = filtered();
        int top = 34;
        int rows = Math.max(1, (height - top) / rowH);
        scroll = Math.max(0, Math.min(list.size() - rows, scroll));
        Set<String> wl = DepthGradientWandItem.getWhitelist(held()).stream()
                .map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        for (int i = 0; i < rows; i++) {
            int idx = scroll + i;
            if (idx >= list.size()) break;
            String id = list.get(idx);
            boolean on = wl.contains(id.toLowerCase(Locale.ROOT));
            int y = top + i * rowH;
            if (on) g.fill(8, y, Math.min(220, width - 8), y + rowH - 1, 0x5033AA55);
            g.drawString(font, (on ? "[x] " : "[ ] ") + id, 10, y, on ? 0x55FFAA : 0xAAAAAA);
        }
        g.drawString(font, "ESC or click [Done] to return", 8, height - 14, 0x666666);
        drawBtn(g, width - 60, height - 16, 52, 12, "Done");
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
        if (my >= height - 16 && my < height - 4 && mx >= width - 60 && mx < width - 8) {
            onClose();
            return true;
        }
        if (my < 34) return super.mouseClicked(mx, my, button);

        List<String> list = filtered();
        int idx = scroll + (int) ((my - 34) / rowH);
        if (idx < 0 || idx >= list.size()) return super.mouseClicked(mx, my, button);
        if (mx < 8 || mx >= Math.min(220, width - 8)) return super.mouseClicked(mx, my, button);

        ItemStack held = held();
        if (held.isEmpty()) return super.mouseClicked(mx, my, button);
        List<String> wl = DepthGradientWandItem.getWhitelist(held);
        String id = list.get(idx);
        boolean present = wl.stream().anyMatch(s -> s.equalsIgnoreCase(id));
        if (present) {
            wl.removeIf(s -> s.equalsIgnoreCase(id));
        } else {
            wl.add(id);
        }
        DepthGradientWandItem.setWhitelist(held, wl);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        List<String> list = filtered();
        int rows = Math.max(1, (height - 34) / rowH);
        scroll = Math.max(0, Math.min(list.size() - rows, scroll - (int) delta));
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(new DepthWandPickerScreen());
    }
}