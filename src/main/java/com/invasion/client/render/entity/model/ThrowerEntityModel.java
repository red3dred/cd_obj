package com.invasion.client.render.entity.model;

import com.invasion.entity.ThrowerEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class ThrowerEntityModel extends BipedEntityModel<ThrowerEntity> {

    public ThrowerEntityModel(ModelPart root) {
        super(root);
    }

    public static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(16, 14).cuboid(-2, -2, -2, 4, 2, 4, dilation), ModelTransform.pivot(0, 16 + pivotOffsetY, 4));
        root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(16, 14).cuboid(-2, -2, -2, 4, 2, 4, dilation.add(0.5F)), ModelTransform.pivot(0, 16 + pivotOffsetY, 4));
        root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
                .uv(1, 1).cuboid(-6, 2, -2, 12, 4, 9, dilation)
                .uv(0, 23).cuboid(-6, 0, 0, 12, 2, 7, dilation), ModelTransform.pivot(-0.4F, 16 + pivotOffsetY, 3));
        root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create()
                .uv(39, 22).cuboid(-3, 0, -1.466667F, 3, 7, 3, dilation), ModelTransform.pivot(-6.5F, 16 + pivotOffsetY, 5));
        root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create()
                .uv(40, 16).mirrored().cuboid(0, 0, 0, 2, 4, 2, dilation), ModelTransform.pivot(5, 16 + pivotOffsetY, 5));
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create()
                .uv(0, 14).cuboid(-2, 0, -2, 4, 2, 4, dilation), ModelTransform.pivot(-4.066667F, 22 + pivotOffsetY, 4));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create()
                .uv(0, 14).mirrored().cuboid(-2, 0, -2, 4, 2, 4, dilation), ModelTransform.pivot(3, 22 + pivotOffsetY, 4));
        return data;
    }

    public static TexturedModelData getTexturedModelData() {
        return TexturedModelData.of(getModelData(Dilation.NONE, 0), 64, 32);
    }

    @Override
    public void setAngles(ThrowerEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.hat.visible = false;
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        head.setPivot(0, 16, 1);
        hat.setPivot(0, 16, 1);
        body.setPivot(0, 16, -3);
        rightLeg.setPivot(-3, 22, 0);
        leftLeg.setPivot(3, 22, 0);
        leftArm.setPivot(6, 16, -1);
        rightArm.setPivot(-6, 16, 0);
    }
}