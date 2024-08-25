package com.invasion.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.invasion.entity.pathfinding.path.ActionablePathNode;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;

@Mixin(DebugRenderer.class)
abstract class DebugRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void invasion_after_render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
        MinecraftClient.getInstance().debugRenderer.pathfindingDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
    }
}

@Mixin(PathfindingDebugRenderer.class)
abstract class PathfindingDebugRendererMixin {
    @Shadow
    private static float getManhattanDistance(BlockPos pos, double x, double y, double z) {
        return (float)(Math.abs(pos.getX() - x) + Math.abs(pos.getY() - y) + Math.abs(pos.getZ() - z));
    }

    @Inject(method = "drawPath", at = @At("RETURN"))
    private static void invasion_drawPath(
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, Path path,
            float nodeSize, boolean drawDebugNodes, boolean drawLabels,
            double cameraX, double cameraY, double cameraZ,
            CallbackInfo info) {
        if (drawLabels) {
            for (int i = 0; i < path.getLength(); ++i) {
                PathNode node = path.getNode(i);
                if (getManhattanDistance(node.getBlockPos(), cameraX, cameraY, cameraZ) <= 80) {
                    DebugRenderer.drawString(matrices, vertexConsumers,
                            String.valueOf(ActionablePathNode.getAction(node)),
                            node.x + 0.5, node.y + 0.75 + 0.75, node.z + 0.5,
                            Colors.WHITE, 0.02F, true, 0, true
                    );
                }
            }
        }
    }
}