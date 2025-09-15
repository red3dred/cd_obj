package com.invasion.entity.ai.goal;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class MobMeleeAttackGoal extends MeleeAttackGoal {
    private int ticks;

    public MobMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
    }

    @Override
    public void start() {
        super.start();
        ticks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        mob.setAttacking(false);
    }

    @Override
    public void tick() {
        super.tick();
        mob.setAttacking(++ticks >= 5 && getCooldown() < getMaxCooldown() / 2);
    }
}