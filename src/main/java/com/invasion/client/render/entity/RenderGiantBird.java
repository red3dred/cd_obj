package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.client.render.ModelVulture;
import com.invasion.entity.EntityIMBird;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class RenderGiantBird extends LivingEntityRenderer<EntityIMBird, ModelVulture> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/vulture.png");

	public RenderGiantBird(EntityRendererFactory.Context ctx) {
		super(ctx, new ModelVulture(ModelVulture.getTexturedModelData().createModel()), 0.4F);
	}

	@Override
    public Identifier getTexture(EntityIMBird entity) {
		return TEXTURE;
	}
}