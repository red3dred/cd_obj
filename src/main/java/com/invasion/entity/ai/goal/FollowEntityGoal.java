package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.NexusEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class FollowEntityGoal<T extends LivingEntity> extends MoveToEntityGoal<T> {
    private final float followDistanceSq;

    @SuppressWarnings("unchecked")
    public FollowEntityGoal(EntityIMLiving entity, float followDistance) {
        this(entity, (Class<T>)LivingEntity.class, followDistance);
    }

    public FollowEntityGoal(EntityIMLiving entity, Class<? extends T> target, float followDistance) {
        super(entity, target);
        followDistanceSq = MathHelper.square(followDistance);
    }

    @Override
    public void start() {
        if (mob instanceof NexusEntity n) {
            n.onFollowingEntity(getTarget());
        }
        super.start();
    }

    @Override
    public void stop() {
        if (mob instanceof NexusEntity n) {
            n.onFollowingEntity(null);
        }
        super.stop();
    }

    @Override
    public void tick() {
        super.tick();
        Entity target = getTarget();
        if (target != null && mob.squaredDistanceTo(target) < followDistanceSq) {
            navigation.haltForTick();
        }
    }
}