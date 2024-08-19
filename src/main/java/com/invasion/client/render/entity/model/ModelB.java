package com.invasion.client.render.entity.model;

import com.invasion.entity.VultureEntity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class ModelB extends SinglePartEntityModel<VultureEntity> {
    private final ModelPart root;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart outerRightWing;
    private final ModelPart outerLeftWing;

    public ModelB(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightWing = root.getChild("right_wing");
        this.leftWing = root.getChild("left_wing");
        this.outerRightWing = rightWing.getChild("outer");
        this.outerLeftWing = leftWing.getChild("outer");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        ModelPartData batHead = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-3, -3, -3, 6, 6, 6), ModelTransform.NONE);
        batHead.addChild("right_ear", ModelPartBuilder.create().uv(24, 0).cuboid(-4, -6, -2, 3, 4, 1), ModelTransform.NONE);
        batHead.addChild("left_ear", ModelPartBuilder.create().uv(24, 0).cuboid(1, -6, -2, 3, 4, 1).mirrored(), ModelTransform.NONE);
        ModelPartData batBody = root.addChild("body", ModelPartBuilder.create()
                .uv(0, 16).cuboid(-3, 4, -3, 6, 12, 6)
                .uv(0, 34).cuboid(-5, 16, 0, 10, 6, 1), ModelTransform.NONE);
        batBody
            .addChild("right_wing", ModelPartBuilder.create().uv(42, 0).cuboid(-12, 1, 1.5F, 10, 16, 1), ModelTransform.NONE)
            .addChild("outer", ModelPartBuilder.create().uv(24, 16).cuboid(-8, 1, 0, 8, 12, 1), ModelTransform.pivot(-12, 1, 1.5F));
        batBody
            .addChild("left_wing", ModelPartBuilder.create().uv(42, 0).cuboid(2, 1, 1.5F, 10, 16, 1).mirrored(), ModelTransform.NONE)
            .addChild("outer", ModelPartBuilder.create().uv(24, 16).cuboid(0, 1, 0, 8, 12, 1).mirrored(), ModelTransform.pivot(12, 1, 1.5F));

        return TexturedModelData.of(data, 64, 64);
    }

    public int getBatSize() {
        return 36;
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(VultureEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        head.setAngles((headPitch / 57.295776F), (headYaw / 57.295776F), 0);
        body.pitch = (0.7853982F + MathHelper.cos(animationProgress * 0.1F) * 0.15F);
        rightWing.yaw = (MathHelper.cos(animationProgress * 1.3F) * MathHelper.PI * 0.25F);
        leftWing.yaw = -rightWing.yaw;
        outerRightWing.yaw = rightWing.yaw * 0.5F;
        outerLeftWing.yaw = -rightWing.yaw * 0.5F;
    }
}