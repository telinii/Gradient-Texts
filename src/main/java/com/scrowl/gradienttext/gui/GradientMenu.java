package com.scrowl.gradienttext.gui;

import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientDirection;
import com.scrowl.gradienttext.gradient.GradientMode;
import com.scrowl.gradienttext.network.NetworkHandler;
import com.scrowl.gradienttext.network.GradientApplyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GradientMenu extends AbstractContainerMenu {
    public static final String TITLE = "container.gradienttext.gradient";

    private static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GradientTextMod.MOD_ID);

    public static final RegistryObject<MenuType<GradientMenu>> MENU_TYPE =
            REGISTRY.register("gradient_menu", () -> IForgeMenuType.create((containerId, playerInventory, data) ->
                    new GradientMenu(containerId, playerInventory)));

    private final ContainerLevelAccess levelAccess;
    private int[] currentColors = new int[]{0xFF0000, 0x00FF00};
    private GradientDirection currentDirection = GradientDirection.HORIZONTAL;
    private GradientMode currentMode = GradientMode.STATIC;
    private boolean currentBold = false;
    private float currentSpeed = GradientData.DEFAULT_SPEED;

    public GradientMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public GradientMenu(int containerId, Inventory playerInventory, ContainerLevelAccess levelAccess) {
        super(MENU_TYPE.get(), containerId);
        this.levelAccess = levelAccess;

        addSlot(new Slot(playerInventory, playerInventory.selected, 8, 8) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int[] getCurrentColors() { return currentColors; }
    public void setCurrentColors(int[] colors) { this.currentColors = colors; }
    public GradientDirection getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(GradientDirection direction) { this.currentDirection = direction; }
    public GradientMode getCurrentMode() { return currentMode; }
    public void setCurrentMode(GradientMode mode) { this.currentMode = mode; }
    public boolean isCurrentBold() { return currentBold; }
    public void setCurrentBold(boolean bold) { this.currentBold = bold; }
    public float getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(float speed) { this.currentSpeed = Math.max(GradientData.MIN_SPEED, Math.min(GradientData.MAX_SPEED, speed)); }

    public void applyToHeldItemClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack heldItem = mc.player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                GradientData data = new GradientData(currentColors, currentDirection, currentMode, currentBold, currentSpeed);
                GradientData.setOnItemStack(heldItem, data);
                NetworkHandler.CHANNEL.sendToServer(new GradientApplyPacket(currentColors, currentDirection, currentMode, currentBold, currentSpeed));
            }
        }
    }

    public void applyToHeldItem(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.isEmpty()) {
            GradientData data = new GradientData(currentColors, currentDirection, currentMode, currentBold, currentSpeed);
            GradientData.setOnItemStack(heldItem, data);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}
