package com.invasion.entity.ai.goal;

import com.invasion.entity.HasAiGoals;
import com.invasion.entity.NexusEntity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class NoNexusPathGoal extends PredicatedGoal {
	private static final float PATH_DISTANCE_TRIGGER = 4;

	public <E extends MobEntity & NexusEntity> NoNexusPathGoal(E entity, Goal goal) {
		super(goal, () -> isLostPathToNexus(entity));
	}

	public static boolean isLostPathToNexus(NexusEntity entity) {
	    return entity.getNavigatorNew().hasGoal(HasAiGoals.Goal.BREAK_NEXUS)
            && entity.getNavigatorNew().getLastPathDistanceToTarget() > PATH_DISTANCE_TRIGGER;
	}
}