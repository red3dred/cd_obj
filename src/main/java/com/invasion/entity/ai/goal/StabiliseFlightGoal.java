package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.IHasAiGoals;
import com.invasion.entity.pathfinding.INavigationFlying;

import net.minecraft.entity.ai.goal.Goal;

public class StabiliseFlightGoal extends Goal {
    private static final int INITIAL_STABILISE_TIME = 50;

    private final EntityIMFlying theEntity;
    private final int stabiliseTime;

    private int time;

    public StabiliseFlightGoal(EntityIMFlying entity, int stabiliseTime) {
        theEntity = entity;
        this.stabiliseTime = stabiliseTime;
    }

    @Override
    public boolean canStart() {
        return theEntity.hasGoal(IHasAiGoals.Goal.STABILISE);
    }

    @Override
    public boolean shouldContinue() {
        if (time < stabiliseTime) {
            return true;
        }

        theEntity.transitionAIGoal(IHasAiGoals.Goal.NONE);
        ((INavigationFlying)theEntity.getNavigatorNew()).setPitchBias(0, 0);
        return false;
    }

    @Override
    public void start() {
        time = 0;
        INavigationFlying nav = (INavigationFlying)theEntity.getNavigatorNew();
        nav.clearPath();
        nav.setMovementType(INavigationFlying.MoveType.PREFER_FLYING);
        nav.setPitchBias(20.0F, 0.5F);
    }

    @Override
    public void tick() {
        if (++time == INITIAL_STABILISE_TIME) {
            ((INavigationFlying)theEntity.getNavigatorNew()).setPitchBias(0, 0);
        }
    }
}