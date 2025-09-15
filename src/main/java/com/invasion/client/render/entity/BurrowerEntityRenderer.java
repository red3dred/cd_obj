package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.BurrowerEntityModel;
import com.invasion.entity.BurrowerEntity;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BurrowerEntityRenderer extends LivingEntityRenderer<BurrowerEntity, BurrowerEntityModel> {
    private static final Identifier TEXTURE = InvasionMod.id("textures/entity/burrower.png");

    public BurrowerEntityRenderer(Context ctx) {
        super(ctx, new BurrowerEntityModel(BurrowerEntityModel.getTexturedModelData2().createModel()), 0.5F);
    }

    @Override
    protected void scale(BurrowerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(2.2F, 2.2F, 2.2F);
    }

    @Override
    public Identifier getTexture(BurrowerEntity entity) {
        return TEXTURE;
    }
}