package com.scrowl.gradienttext;

import com.scrowl.gradienttext.command.GradientCommand;
import com.scrowl.gradienttext.config.ConfigHandler;
import com.scrowl.gradienttext.config.ConfigManager;
import com.scrowl.gradienttext.config.ConfiguredIntegration;
import com.scrowl.gradienttext.gui.GradientMenu;
import com.scrowl.gradienttext.handler.AnimationHandler;
import com.scrowl.gradienttext.handler.GradientEventHandler;
import com.scrowl.gradienttext.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GradientTextMod.MOD_ID)
public class GradientTextMod {
    public static final String MOD_ID = "gradienttext";
    public static final Logger LOGGER = LogManager.getLogger();

    public GradientTextMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GradientEventHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigHandler());
        MinecraftForge.EVENT_BUS.register(GradientCommand.class);

        GradientMenu.register(modEventBus);
        NetworkHandler.register();

        ConfigManager.init();

        ConfiguredIntegration.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Gradient Text Plugin loaded!");
    }
}
