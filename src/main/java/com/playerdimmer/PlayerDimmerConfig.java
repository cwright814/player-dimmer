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

    public int reductionPercentage = PlayerDimmerDefaults.REDUCTION_PERCENTAGE;
    public int maximumPercentage = PlayerDimmerDefaults.MAXIMUM_PERCENTAGE;
    public int minimumPercentage = PlayerDimmerDefaults.MINIMUM_PERCENTAGE;
    public boolean applyToOtherPlayers = PlayerDimmerDefaults.APPLY_TO_OTHER_PLAYERS;

    public int otherEntitiesReductionPercentage = PlayerDimmerDefaults.OTHER_ENTITIES_REDUCTION_PERCENTAGE;
    public int otherEntitiesMaximumPercentage = PlayerDimmerDefaults.OTHER_ENTITIES_MAXIMUM_PERCENTAGE;
    public int otherEntitiesMinimumPercentage = PlayerDimmerDefaults.OTHER_ENTITIES_MINIMUM_PERCENTAGE;
    public boolean applyToOtherEntities = PlayerDimmerDefaults.APPLY_TO_OTHER_ENTITIES;
    public boolean includeItemEntities = PlayerDimmerDefaults.INCLUDE_ITEM_ENTITIES;
    public int maxInterpolationEntities = PlayerDimmerDefaults.MAX_INTERPOLATION_ENTITIES;


    public enum InterpolationMode {
        OFF, FAST, FANCY
    }
    public InterpolationMode interpolationMode = PlayerDimmerDefaults.INTERPOLATION_MODE;
    public float fastModeSpeed = PlayerDimmerDefaults.FAST_MODE_SPEED;
    public float playerMinInterpolationSpeed = PlayerDimmerDefaults.PLAYER_MIN_INTERPOLATION_SPEED;

    public InterpolationMode otherEntitiesInterpolationMode = PlayerDimmerDefaults.OTHER_ENTITIES_INTERPOLATION_MODE;
    public float otherEntitiesFastModeSpeed = PlayerDimmerDefaults.OTHER_ENTITIES_FAST_MODE_SPEED;
    public float otherEntitiesMinInterpolationSpeed = PlayerDimmerDefaults.OTHER_ENTITIES_MIN_INTERPOLATION_SPEED;

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
