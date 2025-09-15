package com.invasion.client.render.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.invasion.entity.ElectricityBoltEntity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class ElectricityBoltEntityRenderer extends EmptyEntityRenderer<ElectricityBoltEntity> {
    public ElectricityBoltEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(ElectricityBoltEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Vector3f[] vertices = entity.getVertices();
        if (vertices != null) {
            matrices.push();
            setupTransform(entity, matrices, tickDelta);
            renderBranches(matrices, vertices, vertexConsumers.getBuffer(RenderLayer.getLightning()));
            matrices.pop();
        }
    }

    private void setupTransform(ElectricityBoltEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw(tickDelta)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getPitch(tickDelta)));
        matrices.scale(0.0625F, 0.0625F, 0.0625F);
    }

    private void renderBranches(MatrixStack matrices, Vector3f[] vertices, VertexConsumer buffer) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float drawWidth = -0.1F;
        for (int pass = 0; pass < 4; pass++) {
            drawWidth += 0.32F;
            for (int i = 1; i < vertices.length; i++) {
                for (int j = 0; j < 5; j++) {
                    float xOffset = 0.5F - drawWidth;
                    float zOffset = 0.5F - drawWidth;
                    if (j == 1 || j == 2) {
                        xOffset += drawWidth * 2;
                    }
                    if (j == 2 || j == 3) {
                        zOffset += drawWidth * 2;
                    }
                    drawBranchSegment(matrix, buffer, vertices[i - 1], vertices[i], 0.5F, 0.5F, 0.6F, xOffset, zOffset);
                }
            }
        }
    }

    private static void drawBranchSegment(Matrix4f matrix, VertexConsumer buffer, Vector3f from, Vector3f to, float red, float green, float blue, float xOffset, float zOffset) {
        buffer.vertex(matrix, from.x + xOffset, from.y * 16, from.z + zOffset).color(red, green, blue, 0.6F);
        buffer.vertex(matrix, to.x + xOffset, to.y * 16, to.z + zOffset).color(red, green, blue, 0.6F);
    }
}