package com.invasion.entity.pathfinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.stream.IntStreams;
import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.nexus.IHasNexus;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.custom.DebugPathCustomPayload;

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

	public IMMobNavigation(EntityIMLiving entity, Actor<?> actor) {
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
        nodeMaker.setCanClimb(true);
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
        }
	}

	@Override
    public Vec3d getPos() {
	    return super.getPos();
	}

    @Override
    public boolean startMovingAlong(Path path, double speed) {
        @Nullable Path previousPath = this.getCurrentPath();
        try {
            stuckTime = 0;
            return super.startMovingAlong(path, speed);
        } finally {
            if (entity instanceof NexusEntity n) {
                n.onPathSet();
            }
            Path currentPath = getCurrentPath();
            if (currentPath != null && currentPath != previousPath) {
                currentPath.getDebugNodeInfos();
                entity.getServer().getPlayerManager().sendToAll(new CustomPayloadS2CPacket(new DebugPathCustomPayload(entity.getId(), createDebuggablePath(currentPath), 0.5F)));
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

    /*
     Pay no mind to what's below. Enjoy this cute little cottage instead
                         (
                            )
                        (            ./\.
                     |^^^^^^^^^|   ./LLLL\.
                     |`.'`.`'`'| ./LLLLLLLL\.
                     |.'`'.'`.'|/LLLL/^^\LLLL\.
                     |.`.''``./LLLL/^ () ^\LLLL\.
                     |.'`.`./LLLL/^  =   = ^\LLLL\.
                     |.`../LLLL/^  _.----._  ^\LLLL\.
                     |'./LLLL/^ =.' ______ `.  ^\LLLL\.
                     |/LLLL/^   /|--.----.--|\ = ^\LLLL\.
                   ./LLLL/^  = |=|__|____|__|=|    ^\LLLL\.
                 ./LLLL/^=     |*|~~|~~~~|~~|*|   =  ^\LLLL\.
               ./LLLL/^        |=|--|----|--|=|        ^\LLLL\.
             ./LLLL/^      =   `-|__|____|__|-' =        ^\LLLL\.
            /LLLL/^   =         `------------'        =    ^\LLLL\
            ~~|.~       =        =      =          =         ~.|~~
              ||     =      =      = ____     =         =     ||
              ||  =               .-'    '-.        =         ||
              ||     _..._ =    .'  .-()-.  '.  =   _..._  =  ||
              || = .'_____`.   /___:______:___\   .'_____`.   ||
              || .-|---.---|-.   ||  _  _  ||   .-|---.---|-. ||
              || |=|   |   |=|   || | || | ||   |=|   |   |=| ||
              || |=|___|___|=|=  || | || | ||=  |=|___|___|=| ||
              || |=|~~~|~~~|=|   || | || | ||   |=|~~~|~~~|=| ||
              || |*|   |   |*|   || | || | ||  =|*|   |   |*| ||
              || |=|---|---|=| = || | || | ||   |=|---|---|=| ||
              || |=|   |   |=|   || | || | ||   |=|   |   |=| ||
              || `-|___|___|-'   ||o|_||_| ||   `-|___|___|-' ||
              ||  '---------`  = ||  _  _  || =  `---------'  ||
              || =   =           || | || | ||      =     =    ||
              ||  %@&   &@  =    || |_||_| ||  =   @&@   %@ = ||
              || %@&@% @%@&@    _||________||_   &@%&@ %&@&@  ||
              ||,,\\V//\\V//, _|___|------|___|_ ,\\V//\\V//,,||
              |--------------|____/--------\____|--------------|
             /- _  -  _   - _ -  _ - - _ - _ _ - _  _-  - _ - _ \
            /____________________________________________________\
     */
    static Path createDebuggablePath(Path path) {
        return new Path(List.of(), BlockPos.ORIGIN, false) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void toBuf(PacketByteBuf buf) {
                buf.writeBoolean(path.reachesTarget());
                buf.writeInt(path.getCurrentNodeIndex());
                buf.writeBlockPos(path.getTarget());

                Set<PathNode> open = new HashSet<>();
                Set<PathNode> closed = new HashSet<>();
                Set<TargetPathNode> targets = new HashSet<>();

                buf.writeCollection(IntStreams.range(path.getLength()).mapToObj(path::getNode).peek(node -> {
                    ((Set)(node instanceof TargetPathNode ? targets : node.visited ? closed : open)).add(node instanceof TargetPathNode t ? t : node);
                }).toList(), (bufx, node) -> node.write(bufx));


                new Path.DebugNodeInfo(open.toArray(PathNode[]::new), closed.toArray(PathNode[]::new), targets).write(buf);
            }
        };
    }
}
