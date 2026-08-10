package com.playerdimmer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PlayerDimmerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "player-dimmer.json");

    public int reductionPercentage = 10;
    public int maximumPercentage = 90;
    public int minimumPercentage = 0;
    public boolean applyToOtherPlayers = true;

    private static PlayerDimmerConfig instance;

    public static PlayerDimmerConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, PlayerDimmerConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                instance = new PlayerDimmerConfig();
            }
        } else {
            instance = new PlayerDimmerConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
