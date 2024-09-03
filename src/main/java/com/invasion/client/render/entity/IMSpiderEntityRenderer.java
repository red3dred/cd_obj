package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.util.Identifier;

import com.invasion.InvasionMod;
import com.invasion.entity.NexusSpiderEntity;

public class IMSpiderEntityRenderer<T extends NexusSpiderEntity> extends SpiderEntityRenderer<T> {
    public static final Identifier JUMPER = InvasionMod.id("textures/entity/spider/jumping_spider.png");
    public static final Identifier MOTHER = InvasionMod.id("textures/entity/spider/mother_spider.png");

    private final Identifier texture;

    public IMSpiderEntityRenderer(EntityRendererFactory.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public Identifier getTexture(T spiderEntity) {
        return texture;
    }
}