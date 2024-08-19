package com.invasion.entity.ai.goal.target;

import com.invasion.entity.EntityIMLiving;
import net.minecraft.entity.ai.goal.RevengeGoal;

public class RetaliateGoal extends RevengeGoal {
	public RetaliateGoal(EntityIMLiving entity) {
		super(entity);
		setGroupRevenge();
	}
}