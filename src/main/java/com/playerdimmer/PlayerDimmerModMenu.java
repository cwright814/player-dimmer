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

            ConfigCategory otherEntities = builder.getOrCreateCategory(Component.literal("Other Entities"));
            
            var applyToOtherEntitiesEntry = entryBuilder.startBooleanToggle(Component.literal("Enable"), PlayerDimmerConfig.get().applyToOtherEntities)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Apply brightness modifier to all other entities."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().applyToOtherEntities = newValue)
                    .build();
            otherEntities.addEntry(applyToOtherEntitiesEntry);

            var otherEntitiesInterpolationEntry = entryBuilder.startEnumSelector(Component.literal("Interpolation Mode"), PlayerDimmerConfig.InterpolationMode.class, PlayerDimmerConfig.get().otherEntitiesInterpolationMode)
                    .setDefaultValue(PlayerDimmerConfig.InterpolationMode.FAST)
                    .setTooltip(Component.literal("Smooth brightness interpolation mode for other entities."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesInterpolationMode = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build();
            otherEntities.addEntry(otherEntitiesInterpolationEntry);

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Reduction (%)"), PlayerDimmerConfig.get().otherEntitiesReductionPercentage, 0, 100)
                    .setDefaultValue(0)
                    .setTooltip(Component.literal("Reduces the brightness intensity by this percentage."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesReductionPercentage = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Maximum (%)"), PlayerDimmerConfig.get().otherEntitiesMaximumPercentage, 0, 100)
                    .setDefaultValue(100)
                    .setTooltip(Component.literal("Caps the maximum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesMaximumPercentage = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Minimum (%)"), PlayerDimmerConfig.get().otherEntitiesMinimumPercentage, 0, 100)
                    .setDefaultValue(0)
                    .setTooltip(Component.literal("Forces a minimum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesMinimumPercentage = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Interpolation Speed"), (int)(PlayerDimmerConfig.get().otherEntitiesFastModeSpeed * 10), 10, 300)
                    .setDefaultValue(80)
                    .setTooltip(Component.literal("Fade speed multiplier for other entities."))
                    .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesFastModeSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue() && otherEntitiesInterpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Min Interpolation Speed"), (int)(PlayerDimmerConfig.get().otherEntitiesMinInterpolationSpeed * 10), 0, 100)
                    .setDefaultValue(20)
                    .setTooltip(Component.literal("Base interpolation speed for other entities."))
                    .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesMinInterpolationSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue() && otherEntitiesInterpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            var interpolationEntry = entryBuilder.startEnumSelector(Component.literal("Interpolation Mode"), PlayerDimmerConfig.InterpolationMode.class, PlayerDimmerConfig.get().interpolationMode)
                    .setDefaultValue(PlayerDimmerConfig.InterpolationMode.FANCY)
                    .setTooltip(Component.literal("Smooth brightness interpolation mode. OFF: No interpolation. FAST: Velocity-based (fast, illusion of space). FANCY: True 3D spatial interpolation (accurate, heavier)."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().interpolationMode = newValue)
                    .build();
            general.addEntry(interpolationEntry);
            
            general.addEntry(entryBuilder.startIntSlider(Component.literal("Interpolation Speed"), (int)(PlayerDimmerConfig.get().fastModeSpeed * 10), 10, 300)
                    .setDefaultValue(80)
                    .setTooltip(Component.literal("Fade speed multiplier for FAST and FANCY modes."))
                    .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().fastModeSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> interpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Min Interpolation Speed"), (int)(PlayerDimmerConfig.get().playerMinInterpolationSpeed * 10), 0, 100)
                    .setDefaultValue(0)
                    .setTooltip(Component.literal("Base interpolation speed for players."))
                    .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().playerMinInterpolationSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> interpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            return builder.build();
        };
    }
}
