package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.ModelBurrower;
import com.invasion.entity.EntityIMBurrower;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RenderBurrower extends LivingEntityRenderer<EntityIMBurrower, ModelBurrower> {
    private static final Identifier TEXTURE = InvasionMod.id("textures/burrower.png");

    public RenderBurrower(Context ctx) {
        super(ctx, new ModelBurrower(ModelBurrower.getTexturedModelData2().createModel()), 0.5F);
    }

    @Override
    protected void scale(EntityIMBurrower entity, MatrixStack matrices, float amount) {
        matrices.scale(2.2F, 2.2F, 2.2F);
    }

    @Override
    public Identifier getTexture(EntityIMBurrower entity) {
        return TEXTURE;
    }
}