package com.invasion.client.render.entity.model;

import org.joml.Vector3f;

import com.invasion.entity.EntityIMBurrower;
import com.invasion.util.math.PosRotate3D;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class ModelBurrower extends EntityModel<EntityIMBurrower> {
    private static final double POSITION_SCALE = 7.269999980926514D;
    private static final Vec3d POSITION_TRANSFORM = new Vec3d(-POSITION_SCALE, -POSITION_SCALE, POSITION_SCALE);

    private final ModelPart head;
    private final ModelPart evenSegment;
    private final ModelPart oddSegment;

    private PosRotate3D[] segments = {};

    public ModelBurrower(ModelPart root) {
        head = root.getChild(root.hasChild("head") ? "head" : "segment");
        evenSegment = root.hasChild("even_segment") ? root.getChild("even_segment") : head;
        oddSegment = root.hasChild("odd_segment") ? root.getChild("odd_segment") : head;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("segment", ModelPartBuilder.create().cuboid(-2, -2.5F, -2.5F, 4, 5, 5).mirrored(), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 32);
    }

    public static TexturedModelData getTexturedModelData2() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("head", ModelPartBuilder.create().cuboid(-1, -3, -3, 2, 6, 6).mirrored(), ModelTransform.NONE);
        root.addChild("even_segment", ModelPartBuilder.create().cuboid(-0.5F, -3.5F, -3.5F, 2, 7, 7).mirrored(), ModelTransform.NONE);
        root.addChild("odd_segment", ModelPartBuilder.create().cuboid(-0.5F, -2.5F, -2.5F, 2, 5, 5).mirrored(), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 32);
    }

    @Override
    public void animateModel(EntityIMBurrower entity, float limbAngle, float limbDistance, float tickDelta) {
        segments = new PosRotate3D[17];

        segments[0] = new PosRotate3D(
            entity.getPos().multiply(POSITION_TRANSFORM),
            PosRotate3D.lerp(tickDelta, entity.getPrevRotation(), entity.getRotation(), new Vector3f())
        );

        for (int i = 0; i < 16; i++) {
            segments[(i + 1)] = entity.getSegments3DLastTick()[i].lerp(tickDelta, entity.getSegments3D()[i]).multiplyPosition(POSITION_TRANSFORM);
        }
    }

    protected ModelPart getPart(int i) {
        if (i == 0) {
            return head;
        }
        return i % 2 == 0 ? evenSegment : oddSegment;
    }

    @Override
    public void setAngles(EntityIMBurrower entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        for (int i = 0; i < segments.length; i++) {
            ModelPart segment = getPart(i);
            segment.setPivot((float) segments[i].position().x, (float) segments[i].position().y, (float) segments[i].position().z);
            segment.setAngles(segments[i].rotation().x(), segments[i].rotation().y(), segments[i].rotation().z());
            segment.render(matrices, vertices, light, overlay, color);
        }
    }
}