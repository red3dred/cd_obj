package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;

import net.minecraft.entity.ai.goal.Goal;

public class EntityAIStoop extends Goal {
    private final EntityIMLiving theEntity;
    private int updateTimer;
    private boolean stopStoop = true;

    public EntityAIStoop(EntityIMLiving entity) {
        theEntity = entity;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canStart() {
        if (--updateTimer > 0) {
            return false;
        }

        updateTimer = 10;
        return theEntity.getWorld()
                .getBlockState(theEntity.getBlockPos().up(2))
                .blocksMovement();
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