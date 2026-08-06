package com.scrowl.gradienttext.item;

import com.scrowl.gradienttext.GradientTextMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GradientTextMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), GradientTextMod.MOD_ID);

    public static final RegistryObject<Item> GRADIENT_WAND =
            ITEMS.register("gradient_wand", GradientWandItem::new);

    public static final RegistryObject<Item> DEPTH_GRADIENT_WAND =
            ITEMS.register("depth_gradient_wand", DepthGradientWandItem::new);

    public static final RegistryObject<CreativeModeTab> TAB =
            TABS.register("gradienttext", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(GRADIENT_WAND.get()))
                    .title(Component.translatable("itemGroup.gradienttext"))
                    .displayItems((params, output) -> {
                        output.accept(GRADIENT_WAND.get());
                        output.accept(DEPTH_GRADIENT_WAND.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }
}
