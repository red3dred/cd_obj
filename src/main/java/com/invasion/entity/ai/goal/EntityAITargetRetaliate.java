package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import net.minecraft.entity.ai.goal.RevengeGoal;

public class EntityAITargetRetaliate extends RevengeGoal {
	public EntityAITargetRetaliate(EntityIMLiving entity) {
		super(entity);
		setGroupRevenge();
	}
}