package com.sekwah.sekcdevtools.client;

import com.sekwah.sekcdevtools.ChangeWatcher;
import com.sekwah.sekcdevtools.SekCDevTools;
import com.sekwah.sekcdevtools.config.DevConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.LoadingErrorScreen;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SekCDevTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DevToolClientEvents {

    public static boolean firstLoad = true;
    private static ChangeWatcher changeWatcher;

    @SubscribeEvent
    public static void menuChange(ScreenEvent.Opening event) {
        // Added loading error screen to avoid a weird forge issue which doesnt matter.
        if(firstLoad && (event.getScreen() instanceof TitleScreen || event.getScreen() instanceof LoadingErrorScreen) && DevConfig.loadWorld != null && DevConfig.loadWorld.length() > 0) {
            firstLoad = false;
            Minecraft mc = Minecraft.getInstance();
            LevelStorageSource levelstoragesource = Minecraft.getInstance().getLevelSource();
            if(levelstoragesource.levelExists(DevConfig.loadWorld)) {
                mc.createWorldOpenFlows().loadLevel(event.getScreen(), DevConfig.loadWorld);
            } else {
                SekCDevTools.LOGGER.error("No level by the name '{}' exists", DevConfig.loadWorld);
            }
        }
    }

    public static void clientSetup(final FMLClientSetupEvent event) {
        try {
            changeWatcher = new ChangeWatcher(Minecraft.getInstance().getResourcePackDirectory().toFile(), () -> Minecraft.getInstance().reloadResourcePacks());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void serverTickEvent(TickEvent.ClientTickEvent event) {
        if(DevConfig.reloadOnResourceChange) changeWatcher.processEvents();
    }
}
