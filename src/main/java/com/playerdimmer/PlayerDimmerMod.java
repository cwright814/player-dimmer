package com.playerdimmer;

import net.fabricmc.api.ModInitializer;

public class PlayerDimmerMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Load the config on startup
        PlayerDimmerConfig.get();
        System.out.println("Player Brightness Dimmer initialized!");
    }
}
