package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.NexusEntity;

import net.minecraft.entity.ai.goal.Goal;

public class NoNexusPathGoal extends PredicatedGoal {
	private static final float PATH_DISTANCE_TRIGGER = 4;

	public NoNexusPathGoal(EntityIMLiving entity, Goal goal) {
		super(goal, () -> isLostPathToNexus(entity));
	}

	public static boolean isLostPathToNexus(NexusEntity entity) {
	    return entity.getNavigatorNew().getAIGoal() == com.invasion.entity.ai.Goal.BREAK_NEXUS
            && entity.getNavigatorNew().getLastPathDistanceToTarget() > PATH_DISTANCE_TRIGGER;
	}
}