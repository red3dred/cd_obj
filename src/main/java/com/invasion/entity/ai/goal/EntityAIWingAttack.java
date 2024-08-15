package com.invasion.entity.ai.goal;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMBird;

import net.minecraft.entity.LivingEntity;

public class EntityAIWingAttack<T extends LivingEntity> extends EntityAIMeleeAttack<T, EntityIMBird> {
    private EntityIMBird mob;

    public EntityAIWingAttack(EntityIMBird entity, Class<? extends T> targetClass, int attackDelay) {
        super(entity, targetClass, attackDelay);
        mob = entity;
    }

    @Override
    public void tick() {
        if (getAttackTime() == 0) {
            mob.setAttackingWithWings(isInStartMeleeRange());
        }
        super.tick();
    }

    @Override
    public void stop() {
        mob.setAttackingWithWings(false);
    }

    protected boolean isInStartMeleeRange() {
        @Nullable
        LivingEntity target = mob.getTarget();
        return target != null && mob.isInRange(target, mob.getWidth() + 3);
    }
}