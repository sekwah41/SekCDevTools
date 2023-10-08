package com.sekwah.sekcdevtools;

import com.sekwah.sekcdevtools.client.DevToolClientEvents;
import com.sekwah.sekcdevtools.common.DevToolCommonEvents;
import com.sekwah.sekcdevtools.config.DevConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(SekCDevTools.MOD_ID)
@Mod.EventBusSubscriber(modid = SekCDevTools.MOD_ID)
public class SekCDevTools {

    public static final String MOD_ID = "sekcdevtools";

    public static final Logger LOGGER = LogManager.getLogger("SekC Dev Tools");

    public SekCDevTools() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(DevToolClientEvents::clientSetup);

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, DevConfig.MOD_CONFIG, "sekc-dev-tools.toml");
    }


}
