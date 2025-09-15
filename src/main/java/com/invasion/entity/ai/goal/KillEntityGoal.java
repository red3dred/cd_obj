package com.invasion.entity.ai.goal;

import com.invasion.entity.NexusEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;

@Deprecated
public class KillEntityGoal<T extends LivingEntity> extends MoveToEntityGoal<T> {
    private static final float ATTACK_RANGE = 1;

    private int attackDelay;
    private int nextAttack;

    public <E extends PathAwareEntity & NexusEntity> KillEntityGoal(E entity, Class<? extends T> targetClass, int attackDelay) {
        super(entity, targetClass);
        this.attackDelay = attackDelay;
    }

    @Override
    public void tick() {
        super.tick();
        setAttackTime(getAttackTime() - 1);
        Entity target = getTarget();
        if (canAttackEntity(target)) {
            attackEntity(target);
        }
    }

    protected void attackEntity(Entity target) {
        mob.tryAttack(getTarget());
        setAttackTime(getAttackDelay());
    }

    protected boolean canAttackEntity(Entity target) {
        if (getAttackTime() > 0) {
            return false;
        }

        double d = (mob.getWidth() + ATTACK_RANGE);
        return mob.squaredDistanceTo(target) < d * d;
    }

    protected int getAttackTime() {
        return nextAttack;
    }

    protected void setAttackTime(int time) {
        nextAttack = time;
    }

    protected int getAttackDelay() {
        return attackDelay;
    }

    protected void setAttackDelay(int time) {
        attackDelay = time;
    }
}