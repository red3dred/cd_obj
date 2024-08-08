package invmod.common.entity.ai;

import org.jetbrains.annotations.Nullable;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class EntityAITargetRetaliate<T extends LivingEntity> extends EntityAISimpleTarget<T> {
	public EntityAITargetRetaliate(EntityIMLiving entity, Class<? extends T> targetType, float distance) {
		super(entity, targetType, distance);
	}

	@SuppressWarnings("unchecked")
    @Override
	public boolean canStart() {
		LivingEntity target = mob.getTarget();
		if (isValidTarget(target)) {
            setTarget((T)target);
            return true;
        }
		return false;
	}

    @Override
    protected boolean isValidTarget(@Nullable Entity entity) {
	    return entity != null && entity.isAlive()
	            && (mob.squaredDistanceTo(entity) <= getAggroRange())
	            && getTargetType().isAssignableFrom(entity.getClass());
	}
}