package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMZombie;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.NexusEntity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

public class AttackNexusGoal<E extends PathAwareEntity & NexusEntity> extends Goal {
    private E mob;

    private int cooldown;

    public AttackNexusGoal(E mob) {
        this.mob = mob;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStart() {
        if (cooldown == 0 && mob.hasGoal(HasAiGoals.Goal.BREAK_NEXUS) && mob.findDistanceToNexus() > 4) {
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