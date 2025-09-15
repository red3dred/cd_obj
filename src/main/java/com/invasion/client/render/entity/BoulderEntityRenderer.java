package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.BoulderEntityModel;
import com.invasion.entity.BoulderEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BoulderEntityRenderer extends EntityRenderer<BoulderEntity> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/boulder.png");

    private final BoulderEntityModel model = new BoulderEntityModel(BoulderEntityModel.getTexturedModelData().createModel());

    public BoulderEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public void render(BoulderEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(getTexture(entity))), light, 0);
    }

    @Override
    public Identifier getTexture(BoulderEntity entity) {
        return TEXTURE;
    }
}