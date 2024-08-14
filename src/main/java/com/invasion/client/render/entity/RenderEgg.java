package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.ModelEgg;
import com.invasion.entity.EntityIMEgg;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RenderEgg extends EntityRenderer<EntityIMEgg> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/spider_egg.png");

	private final ModelEgg model = new ModelEgg(ModelEgg.getTexturedModelData().createModel());

	public RenderEgg(EntityRendererFactory.Context context) {
	    super(context);
	}

	@Override
    public void render(EntityIMEgg entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
	    model.render(matrices, vertexConsumers.getBuffer(model.getLayer(getTexture(entity))), light, 0);
	}

    @Override
    public Identifier getTexture(EntityIMEgg entity) {
        return TEXTURE;
    }
}