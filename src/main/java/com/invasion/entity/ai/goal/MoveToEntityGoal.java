package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.Navigation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

public class MoveToEntityGoal<T extends LivingEntity> extends Goal {
	protected final PathAwareEntity mob;
	protected final Navigation navigation;
	private final Class<? extends T> targetClass;

	private T target;
	private boolean running;
	private Vec3d lastTargetPos;
	private int cooldown;
	private int pathFailedCount;

	@SuppressWarnings("unchecked")
    public <E extends PathAwareEntity & NexusEntity> MoveToEntityGoal(E entity) {
		this(entity, (Class<T>)LivingEntity.class);
	}

	public <E extends PathAwareEntity & NexusEntity> MoveToEntityGoal(E entity, Class<? extends T> target) {
		this.targetClass = target;
		this.mob = entity;
		navigation = entity.getNavigatorNew();
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
		if (--cooldown <= 0 && !navigation.isWaitingForTask() && running && target.squaredDistanceTo(lastTargetPos) > 1.8) {
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
		if (navigation.startMovingTo(target, 0, 1)) {
			if (navigation.getLastPathDistanceToTarget() > 3) {
				cooldown = 30 + mob.getWorld().getRandom().nextInt(10);
				if (navigation.getPath().getCurrentPathLength() > 2) {
					pathFailedCount = 0;
				} else {
					pathFailedCount++;
				}
			} else {
				cooldown = 10 + mob.getWorld().getRandom().nextInt(10);
				pathFailedCount = 0;
			}
		} else {
			pathFailedCount++;
			cooldown = 40 * pathFailedCount + mob.getWorld().getRandom().nextInt(10);
		}

		lastTargetPos = target.getPos();
	}
}