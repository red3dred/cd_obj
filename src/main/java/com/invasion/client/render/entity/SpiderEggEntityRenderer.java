package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.EggModel;
import com.invasion.entity.SpiderEggEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SpiderEggEntityRenderer extends EntityRenderer<SpiderEggEntity> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/spider_egg.png");

	private final EggModel model = new EggModel(EggModel.getTexturedModelData().createModel());

	public SpiderEggEntityRenderer(EntityRendererFactory.Context context) {
	    super(context);
	}

	@Override
    public void render(SpiderEggEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
	    model.render(matrices, vertexConsumers.getBuffer(model.getLayer(getTexture(entity))), light, 0);
	}

    @Override
    public Identifier getTexture(SpiderEggEntity entity) {
        return TEXTURE;
    }
}