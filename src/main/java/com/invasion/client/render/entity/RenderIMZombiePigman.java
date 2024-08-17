package com.invasion.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

import com.invasion.InvasionMod;
import com.invasion.entity.AbstractIMZombieEntity;

public class RenderIMZombiePigman extends RenderIMZombie {
    static final List<Identifier> TEXTURES = Stream.of(
            "textures/entity/zombie_pigman/zombie_pigman.png",
            "textures/entity/zombie_pigman/zombie_pigman.png",
            "textures/entity/zombie_pigman/zombie_pigman_t3.png"
    ).map(InvasionMod::id).toList();

	public RenderIMZombiePigman(EntityRendererFactory.Context ctx) {
		super(ctx);
	}


    @Override
    protected boolean isBrute(AbstractIMZombieEntity entity) {
        return entity.getTier() == 3;
    }


    @Override
    protected List<Identifier> getTextures() {
        return TEXTURES;
    }
}