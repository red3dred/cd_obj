package com.invasion.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;

/**
 * The class name is a misnomer.
 * This causes zombies to crouch when they have a block over their head.
 */
public class StoopGoal extends Goal {
    private final MobEntity theEntity;
    private int updateTimer;
    private boolean stopStoop = true;

    public StoopGoal(MobEntity entity) {
        theEntity = entity;
    }

    @Override
    public boolean canStart() {
        if (--updateTimer > 0) {
            return false;
        }

        updateTimer = 10;
        return !theEntity.getWorld()
                .getBlockState(theEntity.getBlockPos().up(2))
                .canPathfindThrough(NavigationType.LAND);
    }

    @Override
    public boolean shouldContinue() {
        return !stopStoop;
    }

    @Override
    public void start() {
        theEntity.setSneaking(true);
        stopStoop = false;
    }

    @Override
    public void tick() {
        if (canStart()) {
            theEntity.setSneaking(false);
            stopStoop = true;
        }
    }
}