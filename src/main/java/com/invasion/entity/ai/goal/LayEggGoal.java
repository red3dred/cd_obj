package com.invasion.entity.ai.goal;

import com.invasion.entity.SpiderEggEntity;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.Reproducer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

public class LayEggGoal extends Goal {
    private static final int EGG_LAY_TIME = 45;
    private static final int INITIAL_EGG_DELAY = 25;
    private static final int NEXT_EGG_DELAY = 230;
    private static final int EGG_HATCH_TIME = 125;

    private final PathAwareEntity theEntity;

    private int time;
    private boolean isLaying;
    private int eggCount;

    public LayEggGoal(PathAwareEntity entity, int eggs) {
        theEntity = entity;
        eggCount = eggs;
    }

    public void addEggs(int eggs) {
        eggCount += eggs;
    }

    @Override
    public boolean canStart() {
        return (!(theEntity instanceof HasAiGoals g) || g.getAIGoal() == HasAiGoals.Goal.TARGET_ENTITY)
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
        theEntity.getWorld().spawnEntity(new SpiderEggEntity(
                theEntity,
                theEntity instanceof Reproducer i ? i.getOffspring(null) : new Entity[0],
                EGG_HATCH_TIME
        ));
    }
}