package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.Goal;
import net.minecraft.entity.LivingEntity;

public class EntityAITargetOnNoNexusPath<T extends LivingEntity> extends EntityAISimpleTarget<T> {
	private static final float PATH_DISTANCE_TRIGGER = 4;

	public EntityAITargetOnNoNexusPath(EntityIMLiving entity, Class<? extends T> targetType, float distance) {
		super(entity, targetType, distance);
	}

	@Override
    public boolean canStart() {
	    return hasTaskAvailable() && super.canStart();
	}

	@Override
    public boolean shouldContinue() {
	    return hasTaskAvailable() && super.shouldContinue();
	}

	private boolean hasTaskAvailable() {
	    return getEntity().getAIGoal() == Goal.BREAK_NEXUS && getEntity().getNavigatorNew().getLastPathDistanceToTarget() > PATH_DISTANCE_TRIGGER;
	}
}