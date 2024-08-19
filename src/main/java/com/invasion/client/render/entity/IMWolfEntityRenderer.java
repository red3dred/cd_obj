package com.invasion.client.render.entity;

import com.invasion.InvasionMod;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

public class IMWolfEntityRenderer extends WolfEntityRenderer {
    private static final Identifier TEXTURE = InvasionMod.id("textures/wolf/tame_nexus.png");

	public IMWolfEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
	}

    @Override
    protected void scale(WolfEntity entity, MatrixStack matrices, float amount) {
        float f = 1.3F;
        matrices.scale(f, (2 + f) / 3F, f);
    }

	@Override
	public Identifier getTexture(WolfEntity entity) {
	    // TODO: Wolves have variant textures now
		return TEXTURE;
	}
}