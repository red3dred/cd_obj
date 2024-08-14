package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

import com.invasion.InvasionMod;

public class RenderIMZombiePigman extends RenderIMZombie {
    static final List<Identifier> TEXTURES = Stream.of(
            "textures/pigzombie64x32.png",
            "textures/pigzombie64x32.png",
            "textures/zombiePigmanT3.png"
    ).map(InvasionMod::id).toList();

	public RenderIMZombiePigman(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

    @Override
    protected List<Identifier> getTextures() {
        return TEXTURES;
    }
}