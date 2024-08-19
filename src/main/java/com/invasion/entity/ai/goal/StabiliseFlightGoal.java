package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.FlightNavigation;

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
        return theEntity.hasGoal(HasAiGoals.Goal.STABILISE);
    }

    @Override
    public boolean shouldContinue() {
        if (time < stabiliseTime) {
            return true;
        }

        theEntity.transitionAIGoal(HasAiGoals.Goal.NONE);
        ((FlightNavigation)theEntity.getNavigatorNew()).setPitchBias(0, 0);
        return false;
    }

    @Override
    public void start() {
        time = 0;
        FlightNavigation nav = (FlightNavigation)theEntity.getNavigatorNew();
        nav.stop();
        nav.setMovementType(FlightNavigation.MoveType.PREFER_FLYING);
        nav.setPitchBias(20.0F, 0.5F);
    }

    @Override
    public void tick() {
        if (++time == INITIAL_STABILISE_TIME) {
            ((FlightNavigation)theEntity.getNavigatorNew()).setPitchBias(0, 0);
        }
    }
}