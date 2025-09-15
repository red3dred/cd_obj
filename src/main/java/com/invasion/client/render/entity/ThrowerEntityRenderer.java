package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.ThrowerEntityModel;
import com.invasion.entity.ThrowerEntity;

public class ThrowerEntityRenderer extends BipedEntityRenderer<ThrowerEntity, ThrowerEntityModel> {
	private static final List<Identifier> TEXTURES = Stream.of(
	        "textures/entity/thrower/thrower.png",
	        "textures/entity/thrower/thrower_brute.png"
    ).map(InvasionMod::id).toList();

	public ThrowerEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new ThrowerEntityModel(ThrowerEntityModel.getTexturedModelData().createModel()), 1.5F);
	}

    @Override
    protected void scale(ThrowerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(2.4F, 2.4F, 2.4F);
    }

	@Override
    public Identifier getTexture(ThrowerEntity entity) {
	    int id = entity.getTier() - 1;
	    return TEXTURES.get(id < 0 || id >= TEXTURES.size() ? 0 : id);
	}

}