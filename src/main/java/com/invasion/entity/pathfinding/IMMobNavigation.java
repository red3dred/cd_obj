package com.invasion.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.nexus.IHasNexus;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;

public class IMMobNavigation extends MobNavigation implements Navigation {
    static final int MAX_WAIT_TIME = 2000;
    private Goal currentGoal = Goal.NONE;
    private Goal prevGoal = Goal.NONE;

    private int waitingForNotify;
    private Status lastActionResult = Status.SUCCESS;

    private boolean haltRequested;
    private int stuckTime;

    @Nullable
    private Entity followingEntity;
    private Vec3d lastFollowingEntityPos = Vec3d.ZERO;

    @Deprecated
    private final Actor<?> actor;

	public IMMobNavigation(EntityIMLiving entity, Actor<?> actor) {
	    super(entity, entity.getWorld());
	    this.actor = actor;
	}

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        nodeMaker = new IMLandPathNodeMaker();
        nodeMaker.setCanEnterOpenDoors(true);
        nodeMaker.setCanOpenDoors(true);
        nodeMaker.setCanSwim(true);
        return new EntityDensityAwarePathNodeNavigator(nodeMaker, range);
    }

    @Override
    public Entity getTargetEntity() {
        return followingEntity;
    }

    @Deprecated
    @Override
    public Actor<?> getActor() {
        return actor;
    }

    @Override
    public Goal getAIGoal() {
        return currentGoal;
    }

    @Override
    public Goal getPrevAIGoal() {
        return prevGoal;
    }

    @Override
    public Goal transitionAIGoal(Goal newGoal) {
        prevGoal = currentGoal;
        currentGoal = newGoal;
        return newGoal;
    }

    @Override
    public PathAction getCurrentWorkingAction() {
	    return isIdle() || !(currentPath.getCurrentNode() instanceof ActionablePathNode node) ? PathAction.NONE : node.getAction();
	}

    @Override
    public boolean isWaitingForTask() {
		return waitingForNotify > 0;
	}

	@Override
    public void notifyTask(Status result) {
	    waitingForNotify = 0;
        lastActionResult = result;
	}

    @Override
    public Status getLastActionResult() {
        return lastActionResult;
    }

    @Override
    public float getLastPathDistanceToTarget() {
        if (isIdle()) {
            if (currentPath != null && currentPath.getTarget() != null) {
                return MathHelper.sqrt((float) entity.getBlockPos().getSquaredDistance(currentPath.getTarget()));
            }
            return 0;
        }

        if (currentPath.getLastNode() == null) {
            return 0;
        }

        return currentPath.getLastNode().getDistance(currentPath.getTarget());
    }

    @Override
    public void tick() {
        tickObjectives();
        tickFollowing();

        stuckTime++;

        if (haltRequested || waitingForNotify > 0) {
            haltRequested = false;
            waitingForNotify = Math.max(0, waitingForNotify - 1);
        } else {

            if (entity instanceof NexusEntity e) {
                if (!isIdle() && !currentPath.isFinished() && currentPath.getCurrentNodeIndex() < currentPath.getLength() - 1) {
                    Vec3d currentPos = currentPath.getCurrentNode().getPos();
                    Vec3d nextPos = currentPath.getNodePosition(entity, currentPath.getCurrentNodeIndex() + 1);
                    if (!doesNotCollide(entity, currentPos, nextPos, false)) {
                        if (e.onPathBlocked(currentPath, this)) {
                            waitingForNotify = MAX_WAIT_TIME;
                        }
                    }
                }
            }

            super.tick();
        }
    }

    // TODO: Shouldn't this be a goal instead?
    protected void tickObjectives() {
        if (entity.getTarget() != null) {
            transitionAIGoal(Goal.TARGET_ENTITY);
        } else if (entity instanceof IHasNexus i && i.hasNexus()) {
            transitionAIGoal(Goal.BREAK_NEXUS);
        } else {
            transitionAIGoal(Goal.CHILL);
        }
    }

    // TODO: Shouldn't this be a goal instead?
    protected void tickFollowing() {
        if (followingEntity != null) {
            if (!followingEntity.isAlive()) {
                followingEntity = null;
            } else {
                if (isIdle() || (followingEntity.getPos().distanceTo(lastFollowingEntityPos) / (6 + entity.getPos().distanceTo(lastFollowingEntityPos)) > 0.1D)) {
                    Path newPath = findPathTo(followingEntity, (int)entity.distanceTo(followingEntity) + 1);
                    if (newPath != null && startMovingAlong(newPath, entity.getMovementSpeed())) {
                        lastFollowingEntityPos = followingEntity.getPos();
                    }
                }
            }
        }

    }

	@Override
    protected void continueFollowingPath() {
	    super.continueFollowingPath();
	    PathAction currentAction = getCurrentWorkingAction();
	    if (currentAction != PathAction.NONE) {
	        handlePathAction(currentAction);
	    }
	}

	protected void handlePathAction(PathAction action) {

	}

	@Override
    public Vec3d getPos() {
	    return super.getPos();
	}

    @Override
    public boolean startMovingAlong(Path path, double speed) {
        try {
            stuckTime = 0;
            return super.startMovingAlong(path, speed);
        } finally {
            if (entity instanceof NexusEntity n) {
                n.onPathSet();
            }
        }
    }

    @Override
    public int getStuckTime() {
        return stuckTime;
    }

    @Override
    public void haltForTick() {
        haltRequested = true;
    }

    @Override
    public void autoPathToEntity(Entity target) {
        followingEntity = target;
    }
}
