package com.invasion.client.render.entity.model;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

@Deprecated(since = "unused")
public class ModelQuetzalcoatlus<T extends Entity> extends SinglePartEntityModel<T> {

    private final ModelPart root;

    public ModelQuetzalcoatlus(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        // TODO: Usage of texture mirroring is nonsensical. Left limbs should mirror, right and everything else should not. WHY IS EVERYTHING MIRRORED???
        root.addChild("head", ModelPartBuilder.create().mirrored().cuboid(-2.5F, -3.5F, -11, 5, 7, 11).cuboid(0, -9, -13, 0, 7, 16).cuboid(-2, -2.5F, -21F, 4, 6, 10).cuboid(-1.5F, -1.5F, -30, 3, 5, 9), ModelTransform.pivot(0, 19, -40));
        root.addChild("tail", ModelPartBuilder.create().uv(3, 93).mirrored().cuboid(0, -2, 0, 3, 2, 5), ModelTransform.pivot(-1.5F, 18.5F, 11));
        root.addChild("lower_body", ModelPartBuilder.create().uv(108, 104).mirrored().cuboid(-4.5F, -2, 11, 9, 7, 7), ModelTransform.pivot(0, 18, -7));
        root.addChild("upper_body", ModelPartBuilder.create().uv(95, 81).mirrored().cuboid(-5, -2, 0, 10, 8, 11), ModelTransform.pivot(0, 18, -7));
        root.addChild("neck_1", ModelPartBuilder.create().uv(1, 73).mirrored().cuboid(-2.5F, -5, -11, 5, 7, 10), ModelTransform.pivot(0, 21, -6));
        root.addChild("neck_2", ModelPartBuilder.create().uv(28, 73).mirrored().cuboid(-1, -4, -33, 4, 6, 23), ModelTransform.pivot(-1, 20, -7));
        root.addChild("tail_tip", ModelPartBuilder.create().uv(3, 104).mirrored().cuboid(0, -3, 4F, 2, 2, 3), ModelTransform.pivot(-1, 19.5F, 12));
        root.addChild("left_leg", ModelPartBuilder.create()
                .mirrored().cuboid(0, -2, 8, 2, 2, 14)
                .mirrored(false).cuboid(0, -2, -2, 2, 4, 10).cuboid(-0.5F, -2, 22, 3, 1, 4), ModelTransform.pivot(3, 19, 10));
        root.addChild("right_leg", ModelPartBuilder.create().mirrored().cuboid(-2, -2, -2, 2, 4, 10).cuboid(-2, -2, 8, 2, 2, 14).cuboid(-2.5F, -2, 22, 3, 1, 4), ModelTransform.pivot(-3, 19, 10));
        root.addChild("right_wing", ModelPartBuilder.create().mirrored().cuboid(-8, 0, 3, 9, 1, 29).cuboid(-8, -1.5F, -2, 8, 4, 5), ModelTransform.pivot(-5, 18, -5F))
            .addChild("joint", ModelPartBuilder.create().mirrored().cuboid(-15, -1, -2, 15, 3, 4).cuboid(-15, 0, 2, 15, 1, 30), ModelTransform.pivot(-8F, 0, 0))
            .addChild("joint", ModelPartBuilder.create().mirrored().cuboid(-21, -1, -1, 18, 3, 3).cuboid(-21, 0, 2, 18, 1, 31).cuboid(-57, -0.5F, -1, 36, 2, 2).cuboid(-57F, 0, 1, 36, 1, 32).cuboid(-21, 0, -4, 6, 1, 3), ModelTransform.pivot(-12, 0, -1));
        root.addChild("left_wing", ModelPartBuilder.create().mirrored().cuboid(0, -1.5F, -2, 8, 4, 5).cuboid(-1, 0, 3, 9, 1, 29), ModelTransform.pivot(5F, 18F, -5F))
            .addChild("joint", ModelPartBuilder.create().mirrored().cuboid(0, 0, 2, 15, 1, 30).cuboid(0, -1, -2, 15, 3, 4), ModelTransform.NONE)
            .addChild("joint", ModelPartBuilder.create().mirrored().cuboid(0, -1, -1, 18, 3, 3).cuboid(0, 0, 2, 18, 1, 31).cuboid(18, 0, 1, 36, 1, 32).cuboid(18, -0.5F, -0.5F, 36, 2, 2).cuboid(12, 0, -4, 6, 1, 3), ModelTransform.pivot(15F, 0, -1));
        return TexturedModelData.of(data, 500, 150);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }
}
