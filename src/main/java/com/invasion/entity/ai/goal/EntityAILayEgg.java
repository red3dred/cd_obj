package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMEgg;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.ISpawnsOffspring;
import com.invasion.entity.ai.Goal;

import net.minecraft.entity.Entity;

public class EntityAILayEgg extends net.minecraft.entity.ai.goal.Goal {
    private static final int EGG_LAY_TIME = 45;
    private static final int INITIAL_EGG_DELAY = 25;
    private static final int NEXT_EGG_DELAY = 230;
    private static final int EGG_HATCH_TIME = 125;

    private final EntityIMLiving theEntity;

    private int time;
    private boolean isLaying;
    private int eggCount;

    public EntityAILayEgg(EntityIMLiving entity, int eggs) {
        theEntity = entity;
        eggCount = eggs;
    }

    public void addEggs(int eggs) {
        eggCount += eggs;
    }

    @Override
    public boolean canStart() {
        return theEntity.getAIGoal() == Goal.TARGET_ENTITY
            && eggCount > 0
            && theEntity.getVisibilityCache().canSee(theEntity.getTarget());
    }

    @Override
    public void start() {
        time = INITIAL_EGG_DELAY;
    }

    @Override
    public void tick() {
        if (--time <= 0) {
            if (!isLaying) {
                isLaying = true;
                time = EGG_LAY_TIME;
            } else {
                isLaying = false;
                eggCount--;
                time = NEXT_EGG_DELAY;
                layEgg();
            }
        }
    }

    private void layEgg() {
        theEntity.getWorld().spawnEntity(new EntityIMEgg(
                theEntity,
                theEntity instanceof ISpawnsOffspring i ? i.getOffspring(null) : new Entity[0],
                EGG_HATCH_TIME
        ));
    }
}