package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.ModelImp;
import com.invasion.entity.EntityIMImp;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class RenderImp extends LivingEntityRenderer<EntityIMImp, ModelImp> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/entity/imp.png");

	public RenderImp(EntityRendererFactory.Context ctx) {
		super(ctx, new ModelImp(ModelImp.getTexturedModelData().createModel()), 0.3F);
	}

	@Override
    public Identifier getTexture(EntityIMImp entity) {
		return TEXTURE;
	}
}