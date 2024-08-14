package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;

import net.minecraft.entity.LivingEntity;

public class EntityAILeaderTarget<T extends LivingEntity> extends EntityAISimpleTarget<T> {
	private final EntityIMLiving mob;

	public EntityAILeaderTarget(EntityIMLiving entity, Class<? extends T> targetType, float distance) {
		this(entity, targetType, distance, true);
	}

	public EntityAILeaderTarget(EntityIMLiving entity, Class<? extends T> targetType, float distance, boolean needsLos) {
		super(entity, targetType, distance, needsLos);
		this.mob = entity;
	}

	@Override
    public boolean canStart() {
		return mob.readyToRally() && super.canStart();
	}
}