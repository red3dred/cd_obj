package com.invasion.client.render.entity;

import com.invasion.InvasionMod;
import com.invasion.entity.PigmanEngineerEntity;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class PigmanEngineerEntityRenderer extends BipedEntityRenderer<PigmanEngineerEntity, BipedEntityModel<PigmanEngineerEntity>> {
    private static final Identifier TEXTURE = InvasionMod.id("textures/entity/pigman_engineer.png");

    public PigmanEngineerEntityRenderer(Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public Identifier getTexture(PigmanEngineerEntity entity) {
        return TEXTURE;
    }
}