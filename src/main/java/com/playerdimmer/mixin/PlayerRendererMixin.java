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

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("RETURN"))
    private void onExtractRenderState(Entity entity, net.minecraft.client.renderer.entity.state.EntityRenderState state, float partialTicks, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (entity instanceof Player player) {
            PlayerDimmerConfig config = PlayerDimmerConfig.get();
            
            // Check if it's the main player or other players and if we should apply
            boolean isMainPlayer = player == Minecraft.getInstance().player;
            if (!isMainPlayer && !config.applyToOtherPlayers) {
                return;
            }

            int packedLight = state.lightCoords;
            
            // Extract block and sky light
            int blockLight = (packedLight & 0xFFFF);
            int skyLight = (packedLight >> 16) & 0xFFFF;
            
            int blockLightLevel = blockLight >> 4;
            int skyLightLevel = skyLight >> 4;
            
            // Apply modifiers to block light
            blockLightLevel = applyModifiers(blockLightLevel, config);
            
            // Apply modifiers to sky light
            skyLightLevel = applyModifiers(skyLightLevel, config);
            
            // Repack
            int newBlockLight = blockLightLevel << 4;
            int newSkyLight = skyLightLevel << 4;
            
            int newPackedLight = newBlockLight | (newSkyLight << 16);
            state.lightCoords = newPackedLight;
        }
    }
    
    private int applyModifiers(int lightLevel, PlayerDimmerConfig config) {
        // Apply reduction
        if (config.reductionPercentage > 0) {
            float reductionFactor = 1.0f - (config.reductionPercentage / 100.0f);
            lightLevel = (int) (lightLevel * reductionFactor);
        }
        
        // Apply maximum
        int maxLevel = (int) (15.0f * (config.maximumPercentage / 100.0f));
        if (lightLevel > maxLevel) {
            lightLevel = maxLevel;
        }
        
        // Apply minimum
        int minLevel = (int) (15.0f * (config.minimumPercentage / 100.0f));
        if (lightLevel < minLevel) {
            lightLevel = minLevel;
        }
        
        // Ensure bounds
        if (lightLevel < 0) lightLevel = 0;
        if (lightLevel > 15) lightLevel = 15;
        
        return lightLevel;
    }
}
