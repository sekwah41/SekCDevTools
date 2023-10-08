package com.sekwah.sekcdevtools.common;

import com.google.common.collect.Lists;
import com.sekwah.sekcdevtools.ChangeWatcher;
import com.sekwah.sekcdevtools.SekCDevTools;
import com.sekwah.sekcdevtools.config.DevConfig;
import com.sekwah.sekcdevtools.mixin.common.FolderRepositorySourceAccessor;
import com.sekwah.sekcdevtools.mixin.common.PackRepositoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SekCDevTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DevToolCommonEvents {

    public static final Logger LOGGER = LogManager.getLogger("SekC Dev Tools, Common Events");

    private static ChangeWatcher dataChangeWatcher;
    private static MinecraftServer server;

    private static Collection<String> discoverNewPacks(PackRepository p_138223_, WorldData p_138224_, Collection<String> p_138225_) {
        p_138223_.reload();
        Collection<String> collection = Lists.newArrayList(p_138225_);
        Collection<String> collection1 = p_138224_.getDataConfiguration().dataPacks().getDisabled();

        for(String s : p_138223_.getAvailableIds()) {
            if (!collection1.contains(s) && !collection.contains(s)) {
                collection.add(s);
            }
        }

        return collection;
    }

    @SubscribeEvent
    public static void stopEvent(ServerStartingEvent event) {
        server = event.getServer();
        if(event.getServer().getPackRepository() instanceof PackRepositoryAccessor packRepo) {
            var packSources = packRepo.getSources();
            for(var packSource : packSources) {
                // Only expects one for now and that should be fhe datapacks folder in the world file
                if(packSource instanceof FolderRepositorySourceAccessor folderRepo) {
                    folderRepo.getFolder();
                    try {
                        if(dataChangeWatcher != null) dataChangeWatcher.close();
                        dataChangeWatcher = new ChangeWatcher(folderRepo.getFolder().toFile(), () -> {
                            LOGGER.info("Reloading datapacks");
                            PackRepository packrepository = server.getPackRepository();
                            WorldData worlddata = server.getWorldData();
                            Collection<String> collection = packrepository.getSelectedIds();
                            Collection<String> collection1 = discoverNewPacks(packrepository, worlddata, collection);
                            server.reloadResources(collection1).exceptionally((p_138234_) -> {
                                LOGGER.warn("Failed to execute reload", p_138234_);
                                return null;
                            });
                            // Tell all users on the server that the data packs were reloaded
                            server.getPlayerList().broadcastSystemMessage(Component.literal("Data packs reloaded"), true);
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void stopEvent(ServerStoppedEvent event) {
        try {
            if(dataChangeWatcher != null) dataChangeWatcher.close();
            dataChangeWatcher = null;
        } catch (IOException e) {
            LOGGER.error("Failed to close the data change watcher", e);
        }
    }



    @SubscribeEvent
    public static void clientTickEvent(TickEvent.ServerTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        if(DevConfig.reloadOnDataChange && dataChangeWatcher != null) dataChangeWatcher.processEvents();
    }
}
