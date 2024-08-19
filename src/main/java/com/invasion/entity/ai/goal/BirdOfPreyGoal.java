package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.IHasAiGoals;
import com.invasion.entity.ai.MoveState;
import com.invasion.entity.pathfinding.INavigationFlying;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class BirdOfPreyGoal extends Goal {
    private static final int PATIENCE = 600;
    private static final int MIN_ATTACK_DISTANCE = 10;

    private final EntityIMFlying mob;

    private int timeWithGoal;
    private IHasAiGoals.Goal lastGoal;

    public BirdOfPreyGoal(EntityIMFlying entity) {
        mob = entity;
        lastGoal = entity.getAIGoal();
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void start() {
        timeWithGoal = 0;
    }

    @Override
    public void tick() {
        timeWithGoal++;

        if (mob.getAIGoal() != lastGoal) {
            lastGoal = mob.getAIGoal();
            timeWithGoal = 0;
        }

        LivingEntity lastTarget = mob.getTarget();

        if (lastTarget == null) {
            if (!mob.hasNexus()) {
                if (!mob.hasGoal(IHasAiGoals.Goal.BREAK_NEXUS)) {
                    mob.transitionAIGoal(IHasAiGoals.Goal.BREAK_NEXUS);
                }
            } else if (!mob.hasGoal(IHasAiGoals.Goal.CHILL)) {
                mob.transitionAIGoal(IHasAiGoals.Goal.CHILL);
                mob.getNavigatorNew().clearPath();
                ((INavigationFlying)mob.getNavigatorNew()).setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
                ((INavigationFlying)mob.getNavigatorNew()).setLandingPath();
            }
        } else if (mob.hasAnyGoal(IHasAiGoals.Goal.CHILL, IHasAiGoals.Goal.NONE)) {
            mob.transitionAIGoal(shouldAttackTarget(lastTarget) ? IHasAiGoals.Goal.MELEE_TARGET : IHasAiGoals.Goal.STAY_AT_RANGE);
        }

        if (!mob.hasGoal(IHasAiGoals.Goal.STAY_AT_RANGE) && mob.hasGoal(IHasAiGoals.Goal.MELEE_TARGET) && timeWithGoal > PATIENCE) {
            mob.transitionAIGoal(IHasAiGoals.Goal.STAY_AT_RANGE);
        }
    }

    private boolean shouldAttackTarget(LivingEntity target) {
        return mob.getMoveState() != MoveState.FLYING
                && mob.distanceTo(target) < MIN_ATTACK_DISTANCE
                && mob.getRandom().nextFloat() > 0.3F;
    }
}