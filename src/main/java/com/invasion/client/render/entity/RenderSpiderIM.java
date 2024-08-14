package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.SpiderEyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.util.Identifier;

import java.util.List;

import com.invasion.InvasionMod;
import com.invasion.entity.EntityIMSpider;

/**
 * Copy of SpiderEntityRenderer modified to use different textures
 *
 * @see net.minecraft.client.render.entity.SpiderEntityRenderer
 */
public class RenderSpiderIM extends LivingEntityRenderer<EntityIMSpider, SpiderEntityModel<EntityIMSpider>> {
    private static final Identifier NORMAL = Identifier.ofVanilla("textures/entity/spider/spider.png");

	private static final Identifier JUMPER = InvasionMod.id("textures/spiderT2.png");
	private static final Identifier MOTHER = InvasionMod.id("textures/spiderT2b.png");

	private static final List<Identifier> TEXTURES = List.of(NORMAL, JUMPER, MOTHER);

    public RenderSpiderIM(EntityRendererFactory.Context context) {
        super(context, new SpiderEntityModel<>(context.getPart(EntityModelLayers.SPIDER)), 0.8F);
        addFeature(new SpiderEyesFeatureRenderer<>(this));
    }

    @Override
    protected float getLyingAngle(EntityIMSpider entity) {
        return 180;
    }

    @Override
    public Identifier getTexture(EntityIMSpider entity) {
        int id = entity.getTextureId();
        return TEXTURES.get(id < 0 || id >= TEXTURES.size() ? 0 : id);
    }
}