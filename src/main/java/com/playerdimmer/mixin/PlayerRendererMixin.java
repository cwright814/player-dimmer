package com.playerdimmer.mixin;

import com.playerdimmer.PlayerDimmerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

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
        if (entity == null) return;

        PlayerDimmerConfig config = PlayerDimmerConfig.get();
        boolean isPlayer = entity instanceof Player;
        
        if (isPlayer) {
            Player player = (Player) entity;
            boolean isMainPlayer = player == Minecraft.getInstance().player;
            if (!isMainPlayer && !config.applyToOtherPlayers) return;
        } else {
            if (!config.applyToOtherEntities) return;
        }

        // Config Selection
        PlayerDimmerConfig.InterpolationMode mode = isPlayer ? config.interpolationMode : config.otherEntitiesInterpolationMode;
        float speed = isPlayer ? config.fastModeSpeed : config.otherEntitiesFastModeSpeed;
        int reduction = isPlayer ? config.reductionPercentage : config.otherEntitiesReductionPercentage;
        int maxP = isPlayer ? config.maximumPercentage : config.otherEntitiesMaximumPercentage;
        int minP = isPlayer ? config.minimumPercentage : config.otherEntitiesMinimumPercentage;

        float blockLightLevel;
        float skyLightLevel;
        boolean applyTimeSmoothing = false;

        if (mode == PlayerDimmerConfig.InterpolationMode.FANCY) {
            float[] trilinear = computeTrilinearLight(entity, partialTicks);
            blockLightLevel = trilinear[0];
            skyLightLevel = trilinear[1];
            
            int packedLight = state.lightCoords;
            float baseBlockLight = (packedLight & 0xFFFF) / 16.0f;
            
            net.minecraft.core.BlockPos centerPos = net.minecraft.core.BlockPos.containing(entity.getLightProbePosition(partialTicks));
            float centerBlockLight = entity.level().getLightEngine().getLayerListener(net.minecraft.world.level.LightLayer.BLOCK).getLightValue(centerPos);
            
            if (baseBlockLight > centerBlockLight) {
                blockLightLevel = Math.max(blockLightLevel, baseBlockLight);
                applyTimeSmoothing = true;
            }
        } else {
            int packedLight = state.lightCoords;
            blockLightLevel = (packedLight & 0xFFFF) / 16.0f;
            skyLightLevel = ((packedLight >> 16) & 0xFFFF) / 16.0f;
            
            if (mode == PlayerDimmerConfig.InterpolationMode.FAST) {
                applyTimeSmoothing = true;
            }
        }

        if (mode != PlayerDimmerConfig.InterpolationMode.OFF) {
            java.util.UUID uuid = entity.getUUID();
            long currentTime = System.nanoTime();
            long lastTime = fastModeLastTime.getOrDefault(uuid, currentTime);
            
            float dt = (currentTime - lastTime) / 1000000000.0f;
            if (dt > 0.1f) dt = 0.1f;
            if (dt <= 0.0f) dt = 0.001f;
            
            double dx = entity.getX() - entity.xOld;
            double dy = entity.getY() - entity.yOld;
            double dz = entity.getZ() - entity.zOld;
            float distanceMoved = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            boolean currentState = applyTimeSmoothing;
            boolean previousState = fastModePreviousState.getOrDefault(uuid, currentState);
            
            float progress = fastModeTransitionProgress.getOrDefault(uuid, 1.0f);
            float startBlock = fastModeTransitionStartBlock.getOrDefault(uuid, blockLightLevel);
            float startSky = fastModeTransitionStartSky.getOrDefault(uuid, skyLightLevel);
            
            if (currentState != previousState) {
                progress = 0.0f;
                startBlock = fastModeBlockLight.getOrDefault(uuid, blockLightLevel);
                startSky = fastModeSkyLight.getOrDefault(uuid, skyLightLevel);
                fastModePreviousState.put(uuid, currentState);
                fastModeTransitionStartBlock.put(uuid, startBlock);
                fastModeTransitionStartSky.put(uuid, startSky);
            }
            
            if (progress < 1.0f) {
                float tickVelocity = distanceMoved * 20.0f;
                float transitionRate = (tickVelocity / 4.3f) * 8.0f;
                progress += transitionRate * dt;
                if (progress > 1.0f) progress = 1.0f;
                fastModeTransitionProgress.put(uuid, progress);
            }
            
            float speedMultiplier = distanceMoved * speed;
            float lerpFactor = 1.0f - (float)Math.exp(-3.0f * speedMultiplier * dt);
            
            float prevTargetBlock = fastModeTargetBlock.getOrDefault(uuid, blockLightLevel);
            float prevTargetSky = fastModeTargetSky.getOrDefault(uuid, skyLightLevel);
            
            float targetBlockDiff = Math.abs(blockLightLevel - prevTargetBlock);
            float targetSkyDiff = Math.abs(skyLightLevel - prevTargetSky);
            
            fastModeTargetBlock.put(uuid, blockLightLevel);
            fastModeTargetSky.put(uuid, skyLightLevel);
            
            boolean snap = false;
            float maxDiff = Math.max(targetBlockDiff, targetSkyDiff);
            
            if (maxDiff >= 7.5f) snap = true;
            else if (maxDiff >= 3.0f && distanceMoved <= 0.07f) snap = true;
            
            float timePrevBlock = fastModeTimeBlock.getOrDefault(uuid, blockLightLevel);
            float timePrevSky = fastModeTimeSky.getOrDefault(uuid, skyLightLevel);
            
            if (snap) {
                timePrevBlock = blockLightLevel;
                timePrevSky = skyLightLevel;
            }
            
            boolean overruleCrossfade = (maxDiff >= 7.5f) || (maxDiff >= 3.0f && distanceMoved <= 0.07f);
            if (overruleCrossfade) {
                progress = 1.0f;
                fastModeTransitionProgress.put(uuid, progress);
            }
            
            float timeBlock = timePrevBlock + (blockLightLevel - timePrevBlock) * lerpFactor;
            float timeSky = timePrevSky + (skyLightLevel - timePrevSky) * lerpFactor;
            
            fastModeTimeBlock.put(uuid, timeBlock);
            fastModeTimeSky.put(uuid, timeSky);
            
            float activeBlock = applyTimeSmoothing ? timeBlock : blockLightLevel;
            float activeSky = applyTimeSmoothing ? timeSky : skyLightLevel;
            
            blockLightLevel = startBlock * (1.0f - progress) + activeBlock * progress;
            skyLightLevel = startSky * (1.0f - progress) + activeSky * progress;
        }
        
        java.util.UUID uuid = entity.getUUID();
        fastModeBlockLight.put(uuid, blockLightLevel);
        fastModeSkyLight.put(uuid, skyLightLevel);
        fastModeLastTime.put(uuid, System.nanoTime());
        fastModePreviousState.put(uuid, applyTimeSmoothing);
        
        if (mode == PlayerDimmerConfig.InterpolationMode.OFF) {
            fastModeTimeBlock.put(uuid, blockLightLevel);
            fastModeTimeSky.put(uuid, skyLightLevel);
            fastModeTargetBlock.put(uuid, blockLightLevel);
            fastModeTargetSky.put(uuid, skyLightLevel);
        }
        
        blockLightLevel = applyModifiers(blockLightLevel, reduction, maxP, minP);
        
        int newBlockLight = (int) (blockLightLevel * 16.0f);
        int newSkyLight = (int) (skyLightLevel * 16.0f);
        state.lightCoords = newBlockLight | (newSkyLight << 16);
    }
    
    @org.spongepowered.asm.mixin.Unique
    private float[] computeTrilinearLight(Entity entity, float partialTicks) {
        net.minecraft.world.level.Level level = entity.level();
        net.minecraft.world.level.lighting.LevelLightEngine lightEngine = level.getLightEngine();

        net.minecraft.world.phys.Vec3 pos = entity.getLightProbePosition(partialTicks);
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
    private float applyModifiers(float lightLevel, int reduction, int maxP, int minP) {
        if (reduction > 0) {
            float reductionFactor = 1.0f - (reduction / 100.0f);
            lightLevel = lightLevel * reductionFactor;
        }
        
        float maxLevel = 15.0f * (maxP / 100.0f);
        if (lightLevel > maxLevel) {
            lightLevel = maxLevel;
        }
        
        float minLevel = 15.0f * (minP / 100.0f);
        if (lightLevel < minLevel) {
            lightLevel = minLevel;
        }
        
        if (lightLevel < 0.0f) lightLevel = 0.0f;
        if (lightLevel > 15.0f) lightLevel = 15.0f;
        
        return lightLevel;
    }
}
