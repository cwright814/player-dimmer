package com.playerdimmer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class PlayerDimmerModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Player Brightness Dimmer Config"));

            builder.setSavingRunnable(PlayerDimmerConfig::save);

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("Player Brightness"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Reduction (%)"), PlayerDimmerConfig.get().reductionPercentage, 0, 100)
                    .setDefaultValue(15)
                    .setTooltip(Component.literal("Reduces the brightness intensity by this percentage."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().reductionPercentage = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Maximum (%)"), PlayerDimmerConfig.get().maximumPercentage, 0, 100)
                    .setDefaultValue(70)
                    .setTooltip(Component.literal("Caps the maximum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().maximumPercentage = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Minimum (%)"), PlayerDimmerConfig.get().minimumPercentage, 0, 100)
                    .setDefaultValue(0)
                    .setTooltip(Component.literal("Forces a minimum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().minimumPercentage = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Other Players"), PlayerDimmerConfig.get().applyToOtherPlayers)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Apply brightness modifier to other players as well."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().applyToOtherPlayers = newValue)
                    .build());

            general.addEntry(entryBuilder.startEnumSelector(Component.literal("Interpolation Mode"), PlayerDimmerConfig.InterpolationMode.class, PlayerDimmerConfig.get().interpolationMode)
                    .setDefaultValue(PlayerDimmerConfig.InterpolationMode.FANCY)
                    .setTooltip(Component.literal("Smooth brightness interpolation mode. OFF: No interpolation. FAST: Velocity-based (fast, illusion of space). FANCY: True 3D spatial interpolation (accurate, heavier)."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().interpolationMode = newValue)
                    .build());

            return builder.build();
        };
    }
}
