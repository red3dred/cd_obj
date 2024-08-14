package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class EntityAIFollowEntity<T extends LivingEntity> extends EntityAIMoveToEntity<T> {
    private final float followDistanceSq;

    @SuppressWarnings("unchecked")
    public EntityAIFollowEntity(EntityIMLiving entity, float followDistance) {
        this(entity, (Class<T>)LivingEntity.class, followDistance);
    }

    public EntityAIFollowEntity(EntityIMLiving entity, Class<? extends T> target, float followDistance) {
        super(entity, target);
        followDistanceSq = MathHelper.square(followDistance);
    }

    @Override
    public void start() {
        mob.onFollowingEntity(getTarget());
        super.start();
    }

    @Override
    public void stop() {
        mob.onFollowingEntity(null);
        super.stop();
    }

    @Override
    public void tick() {
        super.tick();
        Entity target = getTarget();
        if (target != null && mob.squaredDistanceTo(target) < followDistanceSq) {
            mob.getNavigatorNew().haltForTick();
        }
    }
}