package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.VultureEntityModel;
import com.invasion.entity.VultureEntity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class RenderGiantBird extends LivingEntityRenderer<VultureEntity, VultureEntityModel> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/vulture.png");

	public RenderGiantBird(EntityRendererFactory.Context ctx) {
		super(ctx, new VultureEntityModel(VultureEntityModel.getTexturedModelData().createModel()), 0.4F);
	}

	@Override
    public Identifier getTexture(VultureEntity entity) {
		return TEXTURE;
	}
}