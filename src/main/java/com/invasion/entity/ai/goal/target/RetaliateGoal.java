package com.invasion.entity.ai.goal.target;

import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class RetaliateGoal extends RevengeGoal {
	public RetaliateGoal(PathAwareEntity entity) {
		super(entity);
		setGroupRevenge();
	}
}