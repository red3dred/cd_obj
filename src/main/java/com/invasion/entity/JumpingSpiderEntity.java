package com.invasion.entity;

import com.invasion.entity.ai.goal.PounceGoal;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;

public class JumpingSpiderEntity extends NexusSpiderEntity {
	public JumpingSpiderEntity(EntityType<JumpingSpiderEntity> type, World world) {
		super(type, world);
	}

    public static DefaultAttributeContainer.Builder createAttributes() {
        return SpiderEntity.createSpiderAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.7F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
                .add(EntityAttributes.GENERIC_GRAVITY, 0.08);
    }

    @Override
    protected float getGlobalScaleMultiplier() {
        return super.getGlobalScaleMultiplier() + 0.1F;
    }

    @Override
    protected void initExtraGoals() {
        goalSelector.add(4, new PounceGoal(this, 0.2F, 1.55F, 18));
    }
}