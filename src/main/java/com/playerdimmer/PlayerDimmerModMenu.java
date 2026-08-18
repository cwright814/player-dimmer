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

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Other Players"), PlayerDimmerConfig.get().applyToOtherPlayers)
                    .setDefaultValue(PlayerDimmerDefaults.APPLY_TO_OTHER_PLAYERS)
                    .setTooltip(Component.literal("Apply brightness modifier to other players as well."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().applyToOtherPlayers = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Reduction (%)"), PlayerDimmerConfig.get().reductionPercentage, 0, 100)
                    .setDefaultValue(PlayerDimmerDefaults.REDUCTION_PERCENTAGE)
                    .setTooltip(Component.literal("Reduces the brightness intensity by this percentage."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().reductionPercentage = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Maximum (%)"), PlayerDimmerConfig.get().maximumPercentage, 0, 100)
                    .setDefaultValue(PlayerDimmerDefaults.MAXIMUM_PERCENTAGE)
                    .setTooltip(Component.literal("Caps the maximum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().maximumPercentage = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Minimum (%)"), PlayerDimmerConfig.get().minimumPercentage, 0, 100)
                    .setDefaultValue(PlayerDimmerDefaults.MINIMUM_PERCENTAGE)
                    .setTooltip(Component.literal("Forces a minimum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().minimumPercentage = newValue)
                    .build());

            ConfigCategory otherEntities = builder.getOrCreateCategory(Component.literal("Other Entities"));
            
            var applyToOtherEntitiesEntry = entryBuilder.startBooleanToggle(Component.literal("Enable"), PlayerDimmerConfig.get().applyToOtherEntities)
                    .setDefaultValue(PlayerDimmerDefaults.APPLY_TO_OTHER_ENTITIES)
                    .setTooltip(Component.literal("Apply brightness modifier to all other entities."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().applyToOtherEntities = newValue)
                    .build();
            otherEntities.addEntry(applyToOtherEntitiesEntry);

            otherEntities.addEntry(entryBuilder.startBooleanToggle(Component.literal("Include Item Entities"), PlayerDimmerConfig.get().includeItemEntities)
                    .setDefaultValue(PlayerDimmerDefaults.INCLUDE_ITEM_ENTITIES)
                    .setTooltip(Component.literal("Include item entities (minecraft:item) in dimming."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().includeItemEntities = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Max Interpolation Entities"), PlayerDimmerConfig.get().maxInterpolationEntities / 10, 1, 101)
                    .setDefaultValue(PlayerDimmerDefaults.MAX_INTERPOLATION_ENTITIES / 10)
                    .setTooltip(Component.literal("Caps the number of entities that use expensive interpolation."))
                    .setTextGetter(value -> value == 101 ? Component.literal("Unlimited") : Component.literal(String.valueOf(value * 10)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().maxInterpolationEntities = newValue * 10)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());


            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Reduction (%)"), PlayerDimmerConfig.get().otherEntitiesReductionPercentage, 0, 100)
                    .setDefaultValue(PlayerDimmerDefaults.OTHER_ENTITIES_REDUCTION_PERCENTAGE)
                    .setTooltip(Component.literal("Reduces the brightness intensity by this percentage."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesReductionPercentage = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Maximum (%)"), PlayerDimmerConfig.get().otherEntitiesMaximumPercentage, 0, 100)
                    .setDefaultValue(PlayerDimmerDefaults.OTHER_ENTITIES_MAXIMUM_PERCENTAGE)
                    .setTooltip(Component.literal("Caps the maximum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesMaximumPercentage = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Minimum (%)"), PlayerDimmerConfig.get().otherEntitiesMinimumPercentage, 0, 100)
                    .setDefaultValue(PlayerDimmerDefaults.OTHER_ENTITIES_MINIMUM_PERCENTAGE)
                    .setTooltip(Component.literal("Forces a minimum brightness."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesMinimumPercentage = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build());

            var otherEntitiesInterpolationEntry = entryBuilder.startEnumSelector(Component.literal("Interpolation Mode"), PlayerDimmerConfig.InterpolationMode.class, PlayerDimmerConfig.get().otherEntitiesInterpolationMode)
                    .setDefaultValue(PlayerDimmerDefaults.OTHER_ENTITIES_INTERPOLATION_MODE)
                    .setTooltip(Component.literal("Smooth brightness interpolation mode for other entities."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesInterpolationMode = newValue)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue())
                    .build();
            otherEntities.addEntry(otherEntitiesInterpolationEntry);

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Interpolation Speed"), (int)(PlayerDimmerConfig.get().otherEntitiesFastModeSpeed * 10), 10, 300)
                    .setDefaultValue((int)(PlayerDimmerDefaults.OTHER_ENTITIES_FAST_MODE_SPEED * 10))
                    .setTooltip(Component.literal("Fade speed multiplier for other entities."))
                   .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesFastModeSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue() && otherEntitiesInterpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            otherEntities.addEntry(entryBuilder.startIntSlider(Component.literal("Min Interpolation Speed"), (int)(PlayerDimmerConfig.get().otherEntitiesMinInterpolationSpeed * 10), 0, 100)
                    .setDefaultValue((int)(PlayerDimmerDefaults.OTHER_ENTITIES_MIN_INTERPOLATION_SPEED * 10))
                    .setTooltip(Component.literal("Base interpolation speed for other entities."))
                   .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().otherEntitiesMinInterpolationSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> applyToOtherEntitiesEntry.getValue() && otherEntitiesInterpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            var interpolationEntry = entryBuilder.startEnumSelector(Component.literal("Interpolation Mode"), PlayerDimmerConfig.InterpolationMode.class, PlayerDimmerConfig.get().interpolationMode)
                    .setDefaultValue(PlayerDimmerDefaults.INTERPOLATION_MODE)
                    .setTooltip(Component.literal("Smooth brightness interpolation mode. OFF: No interpolation. FAST: Velocity-based (fast, illusion of space). FANCY: True 3D spatial interpolation (accurate, heavier)."))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().interpolationMode = newValue)
                    .build();
            general.addEntry(interpolationEntry);
            
            general.addEntry(entryBuilder.startIntSlider(Component.literal("Interpolation Speed"), (int)(PlayerDimmerConfig.get().fastModeSpeed * 10), 10, 300)
                    .setDefaultValue((int)(PlayerDimmerDefaults.FAST_MODE_SPEED * 10))
                    .setTooltip(Component.literal("Fade speed multiplier for FAST and FANCY modes."))
                   .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().fastModeSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> interpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(Component.literal("Min Interpolation Speed"), (int)(PlayerDimmerConfig.get().playerMinInterpolationSpeed * 10), 0, 100)
                    .setDefaultValue((int)(PlayerDimmerDefaults.PLAYER_MIN_INTERPOLATION_SPEED * 10))
                    .setTooltip(Component.literal("Base interpolation speed for players."))
                   .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10.0f)))
                    .setSaveConsumer(newValue -> PlayerDimmerConfig.get().playerMinInterpolationSpeed = newValue / 10.0f)
                    .setDisplayRequirement(() -> interpolationEntry.getValue() != PlayerDimmerConfig.InterpolationMode.OFF)
                    .build());

            return builder.build();
        };
    }
}
