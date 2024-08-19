package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.EntityIMZombie;
import com.invasion.entity.ai.Goal;

public class EntityAIAttackNexus extends net.minecraft.entity.ai.goal.Goal {
    private EntityIMLiving mob;

    private int cooldown;

    public EntityAIAttackNexus(EntityIMLiving mob) {
        this.mob = mob;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStart() {
        if (cooldown == 0 && mob.getAIGoal() == Goal.BREAK_NEXUS && mob.findDistanceToNexus() > 4) {
            cooldown = 5;
            return false;
        }

        return mob.findDistanceToNexus() <= mob.getWidth();
    }

    @Override
    public void start() {
        cooldown = 40;
    }

    @Override
    public void tick() {
        if (cooldown == 0) {
            if (mob.findDistanceToNexus() <= mob.getWidth()) {
                if (mob instanceof EntityIMZombie) {
                    ((EntityIMZombie) mob).updateAnimation(true);
                }
                mob.getNexus().damage(2);
            }
            cooldown = 20;
            mob.setAttacking(true);
        }
    }

    @Override
    public void stop() {
        mob.setAttacking(true);
    }
}