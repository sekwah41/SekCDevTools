package com.sekwah.sekcdevtools.client;

import com.sekwah.sekcdevtools.SekCDevTools;
import com.sekwah.sekcdevtools.config.DevConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SekCDevTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DevToolClientEvents {

    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void menuChange(GuiScreenEvent.InitGuiEvent event) {
        if(firstLoad && event.getGui() instanceof TitleScreen && DevConfig.loadWorld != null && DevConfig.loadWorld.length() > 0) {
            firstLoad = false;
            Minecraft mc = Minecraft.getInstance();
            LevelStorageSource levelstoragesource = Minecraft.getInstance().getLevelSource();
            if(levelstoragesource.levelExists(DevConfig.loadWorld)) {
                mc.loadLevel(DevConfig.loadWorld);
            } else {
                SekCDevTools.LOGGER.error("No level by the name '{}' exists", DevConfig.loadWorld);
            }
        }
    }
}
