package com.invasion.client.render.entity.model;

import com.invasion.entity.EntityIMBoulder;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

public class ModelBoulder extends SinglePartEntityModel<EntityIMBoulder> {
    private final ModelPart root;

    public ModelBoulder(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("boulder", ModelPartBuilder.create().cuboid(-4, -4, -4, 8, 8, 8), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(EntityIMBoulder entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        root.roll = animationProgress;
        root.pitch = headPitch;
        root.yaw = headYaw;
    }
}