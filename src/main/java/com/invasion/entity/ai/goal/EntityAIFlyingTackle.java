package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.IHasAiGoals;
import com.invasion.entity.ai.MoveState;
import com.invasion.entity.pathfinding.INavigationFlying;

import net.minecraft.entity.LivingEntity;

public class EntityAIFlyingTackle extends net.minecraft.entity.ai.goal.Goal {
    private final EntityIMFlying theEntity;

    public EntityAIFlyingTackle(EntityIMFlying entity) {
        this.theEntity = entity;
    }

    @Override
    public boolean canStart() {
        return theEntity.hasGoal(IHasAiGoals.Goal.TACKLE_TARGET);
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = theEntity.getTarget();
        if (target == null || !target.isAlive()) {
            theEntity.transitionAIGoal(IHasAiGoals.Goal.NONE);
            return false;
        }

        return theEntity.hasGoal(IHasAiGoals.Goal.TACKLE_TARGET);
    }

    @Override
    public void start() {
        LivingEntity target = theEntity.getTarget();
        if (target != null) {
            ((INavigationFlying)theEntity.getNavigatorNew()).setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
        }
    }

    @Override
    public void tick() {
        if (theEntity.getMoveState() != MoveState.FLYING) {
            theEntity.transitionAIGoal(IHasAiGoals.Goal.MELEE_TARGET);
        }
    }
}