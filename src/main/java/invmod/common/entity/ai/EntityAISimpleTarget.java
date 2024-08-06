package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.util.ComparatorDistanceFrom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;

public class EntityAISimpleTarget<T extends LivingEntity> extends Goal {
	private final EntityIMLiving theEntity;
	private LivingEntity targetEntity;
	private Class<T> targetClass;
	private int outOfLosTimer;
	private float distance;
	private boolean needsLos;

	public EntityAISimpleTarget(EntityIMLiving entity, Class<T> targetType, float distance) {
		this(entity, targetType, distance, true);
	}

	public EntityAISimpleTarget(EntityIMLiving entity, Class<T> targetType, float distance, boolean needsLoS) {
		this.theEntity = entity;
		this.targetClass = targetType;
		this.outOfLosTimer = 0;
		this.distance = distance;
		this.needsLos = needsLoS;
		setControls(EnumSet.of(Control.TARGET));
	}

	public EntityIMLiving getEntity() {
		return this.theEntity;
	}

	@Override
    public boolean canStart() {
		if (targetClass == PlayerEntity.class) {
		    @SuppressWarnings("unchecked")
            T entityplayer = (T)theEntity.getWorld().getClosestPlayer(theEntity, distance);
			if (isValidTarget(entityplayer)) {
				targetEntity = entityplayer;
				return true;
			}
		}

		targetEntity = theEntity.getWorld()
		        .<T>getEntitiesByClass(targetClass, theEntity.getBoundingBox().expand(distance, distance / 2F, distance), this::isValidTarget)
		        .stream()
		        .sorted(ComparatorDistanceFrom.ofComparisonEntities(theEntity.getX(), theEntity.getY(), theEntity.getZ()))
		        .findFirst()
		        .orElse(null)
        ;

		return targetEntity != null;
	}

	@Override
	public boolean shouldContinue() {
		LivingEntity entityliving = theEntity.getTarget();
		if (entityliving == null || !entityliving.isAlive() || theEntity.squaredDistanceTo(entityliving) > distance * distance) {
			return false;
		}
		if (needsLos) {
			if (!theEntity.canSee(entityliving)) {
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
		theEntity.setTarget(targetEntity);
		outOfLosTimer = 0;
	}

	@Override
	public void stop() {
		theEntity.setTarget(null);
		targetEntity = null;
	}

	public Class<T> getTargetType() {
		return targetClass;
	}

	public float getAggroRange() {
		return distance;
	}

	protected void setTarget(T entity) {
		this.targetEntity = entity;
	}

	protected boolean isValidTarget(@Nullable T entity) {
		return !(entity == null
                || entity == theEntity
                || !entity.isAlive()
                || entity instanceof PlayerEntity player && player.getAbilities().creativeMode
                || (needsLos && (!theEntity.canSee(entity))));
	}

}