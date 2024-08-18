package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import com.invasion.entity.EntityIMLiving;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

public class EntityAIMoveToEntity<T extends LivingEntity> extends Goal {
	protected final EntityIMLiving mob;
	private final Class<? extends T> targetClass;

	private T target;
	private boolean running;
	private Vec3d lastTargetPos;
	private int cooldown;
	private int pathFailedCount;

	@SuppressWarnings("unchecked")
    public EntityAIMoveToEntity(EntityIMLiving entity) {
		this(entity, (Class<T>)LivingEntity.class);
	}

	public EntityAIMoveToEntity(EntityIMLiving entity, Class<? extends T> target) {
		this.targetClass = target;
		this.mob = entity;
		setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

	@Override
    @SuppressWarnings("unchecked")
    public boolean canStart() {
		if (--cooldown <= 0) {
		    LivingEntity target = mob.getTarget();
			if (target != null && (targetClass.isAssignableFrom(mob.getTarget().getClass()))) {
				this.target = (T)target;
				return true;
			}
		}
		return false;
	}

	@Override
    public boolean shouldContinue() {
	    LivingEntity target = mob.getTarget();
		return target != null && target == this.target;
	}

	@Override
    public void start() {
		running = true;
		setPath();
	}

	@Override
    public void stop() {
		running = false;
	}

	@Override
    public void tick() {
		if (--cooldown <= 0 && (!mob.getNavigatorNew().isWaitingForTask()) && running && target.squaredDistanceTo(lastTargetPos) > 1.8) {
			setPath();
		}
		if (pathFailedCount > 3) {
			mob.getMoveControl().moveTo(target.getX(), target.getY(), target.getZ(), 1);
		}
	}

	@Deprecated
	protected void setTargetMoves(boolean flag) {
		this.running = flag;
	}

	protected T getTarget() {
		return target;
	}

	protected void setPath() {
		if (mob.getNavigatorNew().tryMoveToEntity(target, 0.0F, 1)) {
			if (mob.getNavigatorNew().getLastPathDistanceToTarget() > 3) {
				cooldown = 30 + mob.getRandom().nextInt(10);
				if (mob.getNavigatorNew().getPath().getCurrentPathLength() > 2) {
					pathFailedCount = 0;
				} else {
					pathFailedCount++;
				}
			} else {
				cooldown = 10 + mob.getRandom().nextInt(10);
				pathFailedCount = 0;
			}
		} else {
			pathFailedCount++;
			cooldown = 40 * pathFailedCount + mob.getRandom().nextInt(10);
		}

		lastTargetPos = target.getPos();
	}
}