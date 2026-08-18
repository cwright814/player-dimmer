package com.playerdimmer;

public class PlayerDimmerDefaults {
    public static final int REDUCTION_PERCENTAGE = 15;
    public static final int MAXIMUM_PERCENTAGE = 70;
    public static final int MINIMUM_PERCENTAGE = 0;
    public static final boolean APPLY_TO_OTHER_PLAYERS = true;

    public static final int OTHER_ENTITIES_REDUCTION_PERCENTAGE = 0;
    public static final int OTHER_ENTITIES_MAXIMUM_PERCENTAGE = 100;
    public static final int OTHER_ENTITIES_MINIMUM_PERCENTAGE = 0;
    public static final boolean APPLY_TO_OTHER_ENTITIES = true;
    public static final boolean INCLUDE_ITEM_ENTITIES = true;
    public static final int MAX_INTERPOLATION_ENTITIES = 250;

    public static final PlayerDimmerConfig.InterpolationMode INTERPOLATION_MODE = PlayerDimmerConfig.InterpolationMode.FANCY;
    public static final float FAST_MODE_SPEED = 8.0f;
    public static final float PLAYER_MIN_INTERPOLATION_SPEED = 2.0f;

    public static final PlayerDimmerConfig.InterpolationMode OTHER_ENTITIES_INTERPOLATION_MODE = PlayerDimmerConfig.InterpolationMode.FAST;
    public static final float OTHER_ENTITIES_FAST_MODE_SPEED = 8.0f;
    public static final float OTHER_ENTITIES_MIN_INTERPOLATION_SPEED = 1.5f;
}
