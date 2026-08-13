package com.playerdimmer.mixin;

import com.playerdimmer.PlayerDimmerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class PlayerRendererMixin<T extends Entity> {

    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTargetBlock = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTargetSky = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeBlockLight = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeSkyLight = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Long> fastModeLastTime = new java.util.WeakHashMap<>();
    
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Boolean> fastModePreviousState = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTransitionProgress = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTransitionStartBlock = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTransitionStartSky = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTimeBlock = new java.util.WeakHashMap<>();
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Map<java.util.UUID, Float> fastModeTimeSky = new java.util.WeakHashMap<>();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("RETURN"))
    private void onExtractRenderState(Entity entity, net.minecraft.client.renderer.entity.state.EntityRenderState state, float partialTicks, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (entity instanceof Player player) {
            PlayerDimmerConfig config = PlayerDimmerConfig.get();
            
            // Check if it's the main player or other players and if we should apply
            boolean isMainPlayer = player == Minecraft.getInstance().player;
            if (!isMainPlayer && !config.applyToOtherPlayers) {
                return;
            }

            float blockLightLevel;
            float skyLightLevel;

            boolean applyTimeSmoothing = false;

            if (config.interpolationMode == PlayerDimmerConfig.InterpolationMode.FANCY) {
                // Compute true 3D trilinear interpolation
                float[] trilinear = computeTrilinearLight(player, partialTicks);
                blockLightLevel = trilinear[0];
                skyLightLevel = trilinear[1];
                
                // Account for client-side dynamic lights (e.g. LambDynamicLights) or Vanilla fire
                // which inject boosted values directly into state.lightCoords.
                int packedLight = state.lightCoords;
                float baseBlockLight = (packedLight & 0xFFFF) / 16.0f;
                
                net.minecraft.core.BlockPos centerPos = net.minecraft.core.BlockPos.containing(player.getLightProbePosition(partialTicks));
                float centerBlockLight = player.level().getLightEngine().getLayerListener(net.minecraft.world.level.LightLayer.BLOCK).getLightValue(centerPos);
                
                // If baseBlockLight is strictly higher than the raw world center block light,
                // it means a dynamic lighting mod (or fire) artificially boosted it. Respect that boost!
                if (baseBlockLight > centerBlockLight) {
                    blockLightLevel = Math.max(blockLightLevel, baseBlockLight);
                    applyTimeSmoothing = true;
                }
            } else {
                int packedLight = state.lightCoords;
                blockLightLevel = (packedLight & 0xFFFF) / 16.0f;
                skyLightLevel = ((packedLight >> 16) & 0xFFFF) / 16.0f;
                
                if (config.interpolationMode == PlayerDimmerConfig.InterpolationMode.FAST) {
                    applyTimeSmoothing = true;
                }
            }

            if (config.interpolationMode != PlayerDimmerConfig.InterpolationMode.OFF) {
                java.util.UUID uuid = player.getUUID();
                long currentTime = System.nanoTime();
                long lastTime = fastModeLastTime.getOrDefault(uuid, currentTime);
                
                float dt = (currentTime - lastTime) / 1000000000.0f; // seconds
                if (dt > 0.1f) dt = 0.1f; // cap at 10fps
                if (dt <= 0.0f) dt = 0.001f;
                
                double dx = player.getX() - player.xOld;
                double dy = player.getY() - player.yOld;
                double dz = player.getZ() - player.zOld;
                float distanceMoved = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                
                // Track state transition between Spatial and Time Smoothing
                boolean currentState = applyTimeSmoothing;
                boolean previousState = fastModePreviousState.getOrDefault(uuid, currentState);
                
                float progress = fastModeTransitionProgress.getOrDefault(uuid, 1.0f);
                float startBlock = fastModeTransitionStartBlock.getOrDefault(uuid, blockLightLevel);
                float startSky = fastModeTransitionStartSky.getOrDefault(uuid, skyLightLevel);
                
                if (currentState != previousState) {
                    progress = 0.0f;
                    // The start value of the transition is what was literally rendered last frame
                    startBlock = fastModeBlockLight.getOrDefault(uuid, blockLightLevel);
                    startSky = fastModeSkyLight.getOrDefault(uuid, skyLightLevel);
                    
                    fastModePreviousState.put(uuid, currentState);
                    fastModeTransitionStartBlock.put(uuid, startBlock);
                    fastModeTransitionStartSky.put(uuid, startSky);
                }
                
                if (progress < 1.0f) {
                    float baseRate = 0.5f * dt; // takes 2 seconds if standing perfectly still
                    float moveRate = distanceMoved / 1.075f; // takes 0.25 seconds at walk speed
                    progress += (baseRate + moveRate);
                    if (progress > 1.0f) progress = 1.0f;
                    fastModeTransitionProgress.put(uuid, progress);
                }
                
                // Calculate time-smoothed curve (Mode B)
                float speedMultiplier = distanceMoved * config.fastModeSpeed;
                float lerpFactor = 1.0f - (float)Math.exp(-3.0f * speedMultiplier * dt);
                
                float prevTargetBlock = fastModeTargetBlock.getOrDefault(uuid, blockLightLevel);
                float prevTargetSky = fastModeTargetSky.getOrDefault(uuid, skyLightLevel);
                
                float targetBlockDiff = Math.abs(blockLightLevel - prevTargetBlock);
                float targetSkyDiff = Math.abs(skyLightLevel - prevTargetSky);
                
                fastModeTargetBlock.put(uuid, blockLightLevel);
                fastModeTargetSky.put(uuid, skyLightLevel);
                
                boolean snap = false;
                float maxDiff = Math.max(targetBlockDiff, targetSkyDiff);
                
                if (maxDiff >= 7.5f) {
                    snap = true;
                } else if (maxDiff >= 3.0f && distanceMoved <= 0.07f) {
                    snap = true;
                }
                
                float timePrevBlock = fastModeTimeBlock.getOrDefault(uuid, blockLightLevel);
                float timePrevSky = fastModeTimeSky.getOrDefault(uuid, skyLightLevel);
                
                if (snap) {
                    timePrevBlock = blockLightLevel;
                    timePrevSky = skyLightLevel;
                }
                
                float timeBlock = timePrevBlock + (blockLightLevel - timePrevBlock) * lerpFactor;
                float timeSky = timePrevSky + (skyLightLevel - timePrevSky) * lerpFactor;
                
                fastModeTimeBlock.put(uuid, timeBlock);
                fastModeTimeSky.put(uuid, timeSky);
                
                // Now determine the target for the CURRENT active mode
                float activeBlock = applyTimeSmoothing ? timeBlock : blockLightLevel;
                float activeSky = applyTimeSmoothing ? timeSky : skyLightLevel;
                
                // Finally, cross-fade from the start of the transition to the active mode
                blockLightLevel = startBlock * (1.0f - progress) + activeBlock * progress;
                skyLightLevel = startSky * (1.0f - progress) + activeSky * progress;
            }
            
            // Unconditionally store the final light levels and timestamp so that
            // switching TO Fast mode later snaps seamlessly to the current state.
            java.util.UUID uuid = player.getUUID();
            fastModeBlockLight.put(uuid, blockLightLevel);
            fastModeSkyLight.put(uuid, skyLightLevel);
            fastModeLastTime.put(uuid, System.nanoTime());
            
            if (config.interpolationMode == PlayerDimmerConfig.InterpolationMode.OFF) {
                fastModeTimeBlock.put(uuid, blockLightLevel);
                fastModeTimeSky.put(uuid, skyLightLevel);
                fastModeTargetBlock.put(uuid, blockLightLevel);
                fastModeTargetSky.put(uuid, skyLightLevel);
            }
            
            // Apply modifiers ONLY to block light
            blockLightLevel = applyModifiers(blockLightLevel, config);
            
            // Repack
            int newBlockLight = (int) (blockLightLevel * 16.0f);
            int newSkyLight = (int) (skyLightLevel * 16.0f);
            
            int newPackedLight = newBlockLight | (newSkyLight << 16);
            state.lightCoords = newPackedLight;
        }
    }
    
    @org.spongepowered.asm.mixin.Unique
    private float[] computeTrilinearLight(Player player, float partialTicks) {
        net.minecraft.world.level.Level level = player.level();
        net.minecraft.world.level.lighting.LevelLightEngine lightEngine = level.getLightEngine();

        net.minecraft.world.phys.Vec3 pos = player.getLightProbePosition(partialTicks);
        double xBase = pos.x() - 0.5;
        double yBase = pos.y() - 0.5;
        double zBase = pos.z() - 0.5;

        int x0 = (int) Math.floor(xBase);
        int y0 = (int) Math.floor(yBase);
        int z0 = (int) Math.floor(zBase);

        float xFrac = (float) (xBase - x0);
        float yFrac = (float) (yBase - y0);
        float zFrac = (float) (zBase - z0);

        net.minecraft.core.BlockPos centerPos = net.minecraft.core.BlockPos.containing(pos);

        float blockLight000 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0, y0, z0, level, centerPos);
        float blockLight100 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0 + 1, y0, z0, level, centerPos);
        float blockLight010 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0, y0 + 1, z0, level, centerPos);
        float blockLight110 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0 + 1, y0 + 1, z0, level, centerPos);
        float blockLight001 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0, y0, z0 + 1, level, centerPos);
        float blockLight101 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0 + 1, y0, z0 + 1, level, centerPos);
        float blockLight011 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0, y0 + 1, z0 + 1, level, centerPos);
        float blockLight111 = getLight(lightEngine, net.minecraft.world.level.LightLayer.BLOCK, x0 + 1, y0 + 1, z0 + 1, level, centerPos);

        float blockLight = lerp3(xFrac, yFrac, zFrac, blockLight000, blockLight100, blockLight010, blockLight110, blockLight001, blockLight101, blockLight011, blockLight111);

        float skyLight000 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0, y0, z0, level, centerPos);
        float skyLight100 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0 + 1, y0, z0, level, centerPos);
        float skyLight010 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0, y0 + 1, z0, level, centerPos);
        float skyLight110 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0 + 1, y0 + 1, z0, level, centerPos);
        float skyLight001 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0, y0, z0 + 1, level, centerPos);
        float skyLight101 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0 + 1, y0, z0 + 1, level, centerPos);
        float skyLight011 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0, y0 + 1, z0 + 1, level, centerPos);
        float skyLight111 = getLight(lightEngine, net.minecraft.world.level.LightLayer.SKY, x0 + 1, y0 + 1, z0 + 1, level, centerPos);

        float skyLight = lerp3(xFrac, yFrac, zFrac, skyLight000, skyLight100, skyLight010, skyLight110, skyLight001, skyLight101, skyLight011, skyLight111);

        return new float[]{blockLight, skyLight};
    }
    
    @org.spongepowered.asm.mixin.Unique
    private float getLight(net.minecraft.world.level.lighting.LevelLightEngine engine, net.minecraft.world.level.LightLayer layer, int x, int y, int z, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos centerPos) {
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (state.isSolidRender()) {
            return engine.getLayerListener(layer).getLightValue(centerPos);
        }
        return engine.getLayerListener(layer).getLightValue(pos);
    }
    
    @org.spongepowered.asm.mixin.Unique
    private float lerp3(float dx, float dy, float dz, float c000, float c100, float c010, float c110, float c001, float c101, float c011, float c111) {
        float c00 = c000 + dx * (c100 - c000);
        float c10 = c010 + dx * (c110 - c010);
        float c01 = c001 + dx * (c101 - c001);
        float c11 = c011 + dx * (c111 - c011);
        
        float c0 = c00 + dy * (c10 - c00);
        float c1 = c01 + dy * (c11 - c01);
        
        return c0 + dz * (c1 - c0);
    }

    @org.spongepowered.asm.mixin.Unique
    private float applyModifiers(float lightLevel, PlayerDimmerConfig config) {
        // Apply reduction
        if (config.reductionPercentage > 0) {
            float reductionFactor = 1.0f - (config.reductionPercentage / 100.0f);
            lightLevel = lightLevel * reductionFactor;
        }
        
        // Apply maximum
        float maxLevel = 15.0f * (config.maximumPercentage / 100.0f);
        if (lightLevel > maxLevel) {
            lightLevel = maxLevel;
        }
        
        // Apply minimum
        float minLevel = 15.0f * (config.minimumPercentage / 100.0f);
        if (lightLevel < minLevel) {
            lightLevel = minLevel;
        }
        
        // Ensure bounds
        if (lightLevel < 0.0f) lightLevel = 0.0f;
        if (lightLevel > 15.0f) lightLevel = 15.0f;
        
        return lightLevel;
    }
}
