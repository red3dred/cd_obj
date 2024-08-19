package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.ImpEntityModel;
import com.invasion.entity.ImpEnitty;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class ImpEntityRenderer extends LivingEntityRenderer<ImpEnitty, ImpEntityModel> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/imp.png");

	public ImpEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new ImpEntityModel(ImpEntityModel.getTexturedModelData().createModel()), 0.3F);
	}

	@Override
    public Identifier getTexture(ImpEnitty entity) {
		return TEXTURE;
	}
}