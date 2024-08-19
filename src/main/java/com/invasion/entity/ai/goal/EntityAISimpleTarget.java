package com.invasion.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import com.invasion.entity.EntityIMLiving;
import com.invasion.util.FloatSupplier;

public class EntityAISimpleTarget<T extends LivingEntity> extends ActiveTargetGoal<T> {
	private final FloatSupplier range;

    public EntityAISimpleTarget(EntityIMLiving entity, Class<T> targetType, float range) {
        this(entity, targetType, range, true);
    }

    public EntityAISimpleTarget(EntityIMLiving entity, Class<T> targetType, float range, boolean checkVisibility) {
        this(entity, targetType, () -> range, checkVisibility);
    }

	public EntityAISimpleTarget(EntityIMLiving entity, Class<T> targetType, FloatSupplier range) {
		this(entity, targetType, range, true);
	}

	public EntityAISimpleTarget(EntityIMLiving entity, Class<T> targetType, FloatSupplier range, boolean checkVisibility) {
	    super(entity, targetType, checkVisibility, false);
		this.range = range;
	}

	protected final EntityIMLiving getEntity() {
		return (EntityIMLiving)mob;
	}

    @Override
    protected double getFollowRange() {
        return range == null ? 0 : range.getAsFloat();
    }

    @Override
    protected void findClosestTarget() {
        this.targetPredicate.setBaseMaxDistance(getFollowRange());
        super.findClosestTarget();
    }
}