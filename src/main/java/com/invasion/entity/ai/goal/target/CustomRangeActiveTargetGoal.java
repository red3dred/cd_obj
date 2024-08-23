package com.invasion.entity.ai.goal.target;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import com.invasion.util.FloatSupplier;

public class CustomRangeActiveTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
	private final FloatSupplier range;

    public CustomRangeActiveTargetGoal(MobEntity entity, Class<T> targetType, float range) {
        this(entity, targetType, range, true);
    }

    public CustomRangeActiveTargetGoal(MobEntity entity, Class<T> targetType, float range, boolean checkVisibility) {
        this(entity, targetType, () -> range, checkVisibility);
    }

	public CustomRangeActiveTargetGoal(MobEntity entity, Class<T> targetType, FloatSupplier range) {
		this(entity, targetType, range, true);
	}

	public CustomRangeActiveTargetGoal(MobEntity entity, Class<T> targetType, FloatSupplier range, boolean checkVisibility) {
	    super(entity, targetType, checkVisibility, false);
		this.range = range;
	}

	protected final MobEntity getEntity() {
		return  mob;
	}

    @Override
    protected double getFollowRange() {
        return range == null ? 0 : range.getAsFloat();
    }

    @Override
    protected void findClosestTarget() {
        targetPredicate.setBaseMaxDistance(getFollowRange());
        super.findClosestTarget();
    }
}