package com.invasion.entity.ai.goal;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.ai.goal.Goal;

public class PredicatedGoal extends Goal {
    private final Goal goal;
    private final BooleanSupplier predicate;

    public PredicatedGoal(Goal goal, BooleanSupplier predicate) {
        this.goal = goal;
        this.predicate = predicate;
        setControls(goal.getControls());
    }

    @Override
    public boolean canStart() {
        return predicate.getAsBoolean() && goal.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return predicate.getAsBoolean() && goal.shouldContinue();
    }

    @Override
    public boolean canStop() {
        return !predicate.getAsBoolean() || goal.canStop();
    }

    @Override
    public void start() {
        goal.start();
    }

    @Override
    public void stop() {
        goal.stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return goal.shouldRunEveryTick();
    }

    @Override
    public void tick() {
        goal.tick();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o || o != null && getClass() == o.getClass() ? goal.equals(((PredicatedGoal)o).goal) : false;
    }

    @Override
    public int hashCode() {
        return goal.hashCode();
    }
}
