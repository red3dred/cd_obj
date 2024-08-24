package com.invasion.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.Stunnable;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.nexus.IHasNexus;
import com.invasion.nexus.test.PathingDebugger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;

public class IMMobNavigation extends MobNavigation implements Navigation {
    static final int MAX_WAIT_TIME = 2000;
    private Goal currentGoal = Goal.NONE;
    private Goal prevGoal = Goal.NONE;

    private int waitingForNotify;
    private Status lastActionResult = Status.SUCCESS;

    private int haltingTicks;
    private int stuckTime;

    @Nullable
    private Entity followingEntity;
    private Vec3d lastFollowingEntityPos = Vec3d.ZERO;

    @Deprecated
    private final Actor<?> actor;

	public IMMobNavigation(MobEntity entity, @SuppressWarnings("deprecation") Actor<?> actor) {
	    super(entity, entity.getWorld());
	    this.actor = actor;
	}

	public static void haltNavigation(PathAwareEntity entity, int ticks) {
	    if (entity.getNavigation() instanceof IMMobNavigation navigation) {
	        navigation.haltForTick();
	    }
	}

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        var nodeMaker = new IMLandPathNodeMaker();
        this.nodeMaker = nodeMaker;
        nodeMaker.setCanEnterOpenDoors(true);
        nodeMaker.setCanOpenDoors(true);
        nodeMaker.setCanSwim(true);
        nodeMaker.setCanClimbLadder(true);
        return new DynamicPathNodeNavigator(nodeMaker, range);
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

        if (entity instanceof Stunnable l && l.isStunned()) {
            return;
        }

        if (haltingTicks > 0 || waitingForNotify > 0) {
            InvasionMod.LOGGER.info("{} waiting for task to complete. halting={}, waiting={}", entity, haltingTicks, waitingForNotify);
            haltingTicks = Math.max(0, haltingTicks - 1);
            waitingForNotify = Math.max(0, (waitingForNotify / 2) - 1);
        } else {
            if (entity instanceof NexusEntity e
                    && getCurrentWorkingAction() == PathAction.NONE) {
                if (!isIdle()
                        && !currentPath.isFinished()
                        && currentPath.getCurrentNodeIndex() < currentPath.getLength() - 1
                        && ActionablePathNode.getAction(currentPath.getNode(currentPath.getCurrentNodeIndex() + 1)) == PathAction.NONE
                ) {
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
	    entity.setSneaking(false);
	    if (entity instanceof NexusEntity e) {
            e.setIsHoldingIntoLadder(false);
        }
	    if (currentAction != PathAction.NONE) {
            handlePathAction(currentAction);
	    }
	}

	protected void handlePathAction(PathAction action) {
	    InvasionMod.LOGGER.info("Handling path action {}", action);
        if (action.getType() == PathAction.Type.CLIMB) {
            Vec3d targetPosition = entity.getBlockPos().offset(action.getBuildDirection()).toCenterPos();
            entity.getMoveControl().moveTo(targetPosition.x, targetPosition.y, targetPosition.z, 1);
            if (action.getBuildDirection() == Direction.UP) {
                entity.getJumpControl().setActive();
            } else {
                if (entity instanceof NexusEntity e) {
                    e.setIsHoldingIntoLadder(true);
                }
                entity.setSneaking(true);
                entity.fallDistance = 0;
                entity.setJumping(false);
            }
        } else if (action.getType() != PathAction.Type.DIG) {
            if (entity instanceof NexusEntity e && e.handlePathAction(getCurrentPath().getCurrentNodePos(), action, this)) {
                waitingForNotify = MAX_WAIT_TIME;
            }
        }
	}

	@Override
    public Vec3d getPos() {
	    return super.getPos();
	}

    @Override
    public boolean startMovingAlong(Path path, double speed) {
        @Nullable Path previousPath = getCurrentPath();
        try {
            stuckTime = 0;
            return super.startMovingAlong(path, speed);
        } finally {
            if (entity instanceof NexusEntity n) {
                n.onPathSet();
            }
            Path currentPath = getCurrentPath();
            if (currentPath != null && currentPath != previousPath) {
                PathingDebugger.sendPathToClients(entity, currentPath, 0.5F);
            }
        }
    }

    @Override
    public int getStuckTime() {
        return stuckTime;
    }

    @Override
    public void haltForTick() {
        haltingTicks = Math.max(haltingTicks, 1);
    }

    @Override
    public void autoPathToEntity(Entity target) {
        followingEntity = target;
    }
}
