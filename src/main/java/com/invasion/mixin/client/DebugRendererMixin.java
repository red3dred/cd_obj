package com.invasion.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(DebugRenderer.class)
abstract class DebugRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void invasion_after_render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
        MinecraftClient.getInstance().debugRenderer.pathfindingDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
    }
}
