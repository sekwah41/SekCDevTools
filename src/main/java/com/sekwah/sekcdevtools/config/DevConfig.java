package com.sekwah.sekcdevtools.config;

import com.sekwah.sekcdevtools.SekCDevTools;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SekCDevTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DevConfig {


    public static final String CATEGORY_CLIENT = "client";

    public static final ForgeConfigSpec MOD_CONFIG;

    private static final ForgeConfigSpec.ConfigValue<String> CONFIG_LOAD_WORLD;
    private static final ForgeConfigSpec.ConfigValue<Boolean> CONFIG_RELOAD_ON_RESOURCE_CHANGE;
    public static String loadWorld;
    public static boolean reloadOnResourceChange;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        configBuilder.comment("Variables for client side things").push(CATEGORY_CLIENT);

        CONFIG_LOAD_WORLD = configBuilder.comment("World to load on first start")
                .define("worldFirstLoad", "");

        CONFIG_RELOAD_ON_RESOURCE_CHANGE = configBuilder.comment("Reload resources when there are changes to the resoucepack folder")
                .define("resourcePacksReload", false);

        configBuilder.pop();

        MOD_CONFIG = configBuilder.build();
    }

    public static void loadVariables() {
        loadWorld = CONFIG_LOAD_WORLD.get();
        reloadOnResourceChange = CONFIG_RELOAD_ON_RESOURCE_CHANGE.get();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        loadVariables();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        loadVariables();
    }
}
