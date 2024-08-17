package com.invasion.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMLiving;
import com.invasion.util.FloatSupplier;
import com.invasion.util.math.ComparatorDistanceFrom;

public class EntityAISimpleTarget<T extends LivingEntity> extends Goal {
	protected final EntityIMLiving mob;
	private LivingEntity targetEntity;
	private Class<? extends T> targetClass;
	private int outOfLosTimer;
	private final FloatSupplier distance;
	private boolean needsLos;

    public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends T> targetType, float distance) {
        this(entity, targetType, distance, true);
    }

    public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends T> targetType, float distance, boolean needsLoS) {
        this(entity, targetType, () -> distance, needsLoS);
    }

	public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends T> targetType, FloatSupplier distance) {
		this(entity, targetType, distance, true);
	}

	public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends T> targetType, FloatSupplier distance, boolean needsLoS) {
		this.mob = entity;
		this.targetClass = targetType;
		this.outOfLosTimer = 0;
		this.distance = distance;
		this.needsLos = needsLoS;
		setControls(EnumSet.of(Control.TARGET));
	}

	public EntityIMLiving getEntity() {
		return this.mob;
	}

	@Override
    public boolean canStart() {
	    float distance = this.distance.getAsFloat();
		if (targetClass == PlayerEntity.class) {
		    @SuppressWarnings("unchecked")
            T entityplayer = (T)mob.getWorld().getClosestPlayer(mob, distance);
			if (isValidTarget(entityplayer)) {
				targetEntity = entityplayer;
				return true;
			}
		}

		targetEntity = mob.getWorld()
		        .getEntitiesByClass(targetClass, mob.getBoundingBox().expand(distance, distance / 2F, distance), this::isValidTarget)
		        .stream()
		        .sorted(ComparatorDistanceFrom.ofComparisonEntities(mob.getX(), mob.getY(), mob.getZ()))
		        .findFirst()
		        .orElse(null)
        ;

		return targetEntity != null;
	}

	@Override
	public boolean shouldContinue() {
		LivingEntity entityliving = mob.getTarget();
		if (entityliving == null || !entityliving.isAlive() || mob.squaredDistanceTo(entityliving) > MathHelper.square(distance.getAsFloat())) {
			return false;
		}
		if (needsLos) {
			if (!mob.canSee(entityliving)) {
				if (++outOfLosTimer > 60) {
					return false;
				}
			} else {
				this.outOfLosTimer = 0;
			}
		}

		return true;
	}

	@Override
	public void start() {
		mob.setTarget(targetEntity);
		outOfLosTimer = 0;
	}

	@Override
	public void stop() {
		mob.setTarget(null);
		targetEntity = null;
	}

	public Class<? extends T> getTargetType() {
		return targetClass;
	}

	public float getAggroRange() {
		return distance.getAsFloat();
	}

	protected void setTarget(T entity) {
		this.targetEntity = entity;
	}

	protected boolean isValidTarget(@Nullable Entity entity) {
		return !(entity == null
                || entity == mob
                || !entity.isAlive()
                || entity instanceof PlayerEntity player && player.getAbilities().creativeMode
                || (needsLos && (!mob.canSee(entity))));
	}
}