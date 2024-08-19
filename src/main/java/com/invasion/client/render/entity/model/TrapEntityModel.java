package com.invasion.client.render.entity.model;

import com.invasion.entity.TrapEntity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

public class TrapEntityModel extends SinglePartEntityModel<TrapEntity> {
    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart flames;

    public TrapEntityModel(ModelPart root) {
        this.root = root;
        core = root.getChild("core");
        flames = root.getChild("flames");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("base", ModelPartBuilder.create().uv(0, 23).mirrored().cuboid(0, 0, 0, 4, 1, 2), ModelTransform.pivot(-2, -1, -1));
        root.addChild("base_s1", ModelPartBuilder.create().uv(0, 27).mirrored().cuboid(0, 0, 0, 2, 1, 1), ModelTransform.pivot(-1, -1,  1));
        root.addChild("base_s2", ModelPartBuilder.create().uv(0, 27).mirrored().cuboid(0, 0, 0, 2, 1, 1), ModelTransform.pivot(-1, -1, -2));

        root.addChild("core", ModelPartBuilder.create().uv(0, 13).mirrored().cuboid(0, 0, 0, 4, 2, 4), ModelTransform.pivot(-2, -2, -2));
        root.addChild("flames", ModelPartBuilder.create().uv(5, 7 ).mirrored().cuboid(0, 0, 0, 4, 2, 4), ModelTransform.pivot(-2, -2, -2));

        root.addChild("clasp_1a", ModelPartBuilder.create().uv(0, 0 ).mirrored().cuboid(0, 0, 0, 2, 2, 1), ModelTransform.pivot(-1, -2,  2));
        root.addChild("clasp_2a", ModelPartBuilder.create().uv(0, 0 ).mirrored().cuboid(0, 0, 0, 2, 2, 1), ModelTransform.pivot(-1, -2, -3));

        root.addChild("clasp_2b", ModelPartBuilder.create().uv(0, 7 ).mirrored().cuboid(0, 0, 0, 2, 1, 2), ModelTransform.pivot(-1, -1, -5));
        root.addChild("clasp_1b", ModelPartBuilder.create().uv(0, 7 ).mirrored().cuboid(0, 0, 0, 2, 1, 2), ModelTransform.pivot(-1, -1,  3));

        root.addChild("clasp_3a", ModelPartBuilder.create().uv(0, 3 ).mirrored().cuboid(0, 0, 0, 1, 2, 2), ModelTransform.pivot( 2, -2, -1));
        root.addChild("clasp_4a", ModelPartBuilder.create().uv(0, 3 ).mirrored().cuboid(0, 0, 0, 1, 2, 2), ModelTransform.pivot(-3, -2, -1));

        root.addChild("clasp_3b", ModelPartBuilder.create().uv(0, 19).mirrored().cuboid(0, 0, 0, 2, 1, 2), ModelTransform.pivot( 3, -1, -1));
        root.addChild("clasp_4b", ModelPartBuilder.create().uv(0, 19).mirrored().cuboid(0, 0, 0, 2, 1, 2), ModelTransform.pivot(-5, -1, -1));
        return TexturedModelData.of(data, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(TrapEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        TrapEntity.Type type = entity.getTrapType();
        core.visible = type == TrapEntity.Type.RIFT;
        flames.visible = type == TrapEntity.Type.FIRE;
    }
}