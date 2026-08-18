# Agent Instructions for player-dimmer

Welcome to the `player-dimmer` repository! Please adhere to the following guidelines to maintain consistency and avoid known pitfalls.

## Workflow Utilities

- **Compiling:** Always execute `./build.sh` to compile the `.jar` files. Do not run gradle commands manually.
- **Versioning:** Always execute `./bump-version.sh <version>` to bump or set a new package version. Do not manually edit `gradle.properties` or `fabric.mod.json` to change the version.

## Architecture Notes

- **Config Defaults:** All default config values for both `PlayerDimmerConfig` and `PlayerDimmerModMenu` MUST be referenced from the centralized `PlayerDimmerDefaults.java` file. Do not hardcode slider defaults in the UI.
- **Time Steps (Ticks vs Frames):** Be very careful when using `level.getGameTime()` for logic that runs in the render loop (like `onExtractRenderState`). `getGameTime()` increments only 20 times per second (Ticks), whereas the render loop runs 60-144+ times per second (Frames). Relying on ticks for rendering budgets or `dt` interpolation will cause severe micro-stuttering and flickering. Instead, use `System.nanoTime()` (with a threshold like `> 2_000_000L` nanoseconds) to accurately track render frames natively.
- **Crossfade and Snapping Triggers:** Be careful when modifying the brightness transition logic (such as the 50% / 7.5 maxDiff snap threshold). The smooth crossfade heavily relies on `currentState != previousState` tracking within the `WeakHashMap` caches. Altering the bypass logic or failing to record the previous state properly will easily cause infinite transition loops or blinking artifacts.
