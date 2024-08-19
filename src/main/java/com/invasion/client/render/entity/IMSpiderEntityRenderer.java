package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
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
public class IMSpiderEntityRenderer extends MobEntityRenderer<EntityIMSpider, SpiderEntityModel<EntityIMSpider>> {
    private static final Identifier NORMAL = Identifier.ofVanilla("textures/entity/spider/spider.png");

	private static final Identifier JUMPER = InvasionMod.id("textures/entity/spider/jumping_spider.png");
	private static final Identifier MOTHER = InvasionMod.id("textures/entity/spider/mother_spider.png");

	private static final List<Identifier> TEXTURES = List.of(NORMAL, JUMPER, MOTHER);

    public IMSpiderEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SpiderEntityModel<>(context.getPart(EntityModelLayers.SPIDER)), 0.8F);
        addFeature(new SpiderEyesFeatureRenderer<>(this));
    }

    @Override
    protected float getLyingAngle(EntityIMSpider entity) {
        return 180;
    }

    @Override
    public Identifier getTexture(EntityIMSpider entity) {
        int id = entity.getTier() == 2 ? entity.getFlavour() == 0 ? 1 : 2 : 0;
        return TEXTURES.get(id < 0 || id >= TEXTURES.size() ? 0 : id);
    }
}