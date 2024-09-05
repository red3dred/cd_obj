package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.TrapEntityModel;
import com.invasion.entity.TrapEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class TrapEntityRenderer extends EntityRenderer<TrapEntity> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/trap.png");

	private final TrapEntityModel model;

	public TrapEntityRenderer(EntityRendererFactory.Context ctx) {
	    super(ctx);
	    shadowRadius = 0;
	    model = new TrapEntityModel(TrapEntityModel.getTexturedModelData().createModel());
	}

	@Override
    public void render(TrapEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
	    matrices.push();
	    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
	    matrices.scale(1.3F, 1.3F, 1.3F);
	    model.setAngles(entity, 0, 0, entity.age + tickDelta, 0, 0);
	    model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))), light, 0);
	    matrices.pop();
	}

	@Override
    public Identifier getTexture(TrapEntity entity) {
		return TEXTURE;
	}
}