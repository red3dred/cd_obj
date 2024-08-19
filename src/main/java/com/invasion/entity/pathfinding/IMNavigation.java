package com.invasion.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;
import com.invasion.entity.EntityIMLiving;
import com.invasion.nexus.INexusAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;

public class IMNavigation implements Notifiable, Navigation {
	protected static final int XZPATH_HORIZONTAL_SEARCH = 1;
	protected static final double ENTITY_TRACKING_TOLERANCE = 0.1D;
	protected static final double MINIMUM_PROGRESS = 0.01D;

	protected final EntityIMLiving theEntity;

	protected PathSource pathSource;

	protected Path path;
	@Nullable
	protected PathNode activeNode;

	protected Vec3d entityCentre;

	protected Entity pathEndEntity;
	protected Vec3d pathEndEntityLastPos = Vec3d.ZERO;

	protected float moveSpeed;
	protected float pathSearchLimit;

	protected boolean noSunPathfind;

	protected int totalTicks;
	protected Vec3d lastPos = Vec3d.ZERO;
	private Vec3d holdingPos;
	protected boolean nodeActionFinished = true;
	private boolean canSwim;
	protected boolean waitingForNotify;
	protected boolean actionCleared = true;
	protected double lastDistance;
	protected int ticksStuck;
	private boolean maintainPosOnWait;
	private Status lastActionResult;
	private boolean haltMovement;
	private boolean autoPathToEntity;

    protected Goal currentGoal = Goal.NONE;
    protected Goal prevGoal = Goal.NONE;

	protected final Actor<?> actor;

	public IMNavigation(EntityIMLiving entity, PathSource pathSource) {
		this.theEntity = entity;
		this.pathSource = pathSource;
		actor = createActor(entity);
	}

	@Override
    public Actor<?> getNodeMaker() {
	    return actor;
	}

	protected <T extends Entity> Actor<T> createActor(T entity) {
	    return new Actor<>(entity);
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
	    return !nodeActionFinished && !isIdle() ? activeNode.action : PathAction.NONE;
	}

	protected boolean isMaintainingPos() {
		return maintainPosOnWait;
	}

	protected void setNoMaintainPos() {
		maintainPosOnWait = false;
	}

	protected void setMaintainPosOnWait(Vec3d pos) {
		holdingPos = pos;
		maintainPosOnWait = true;
	}

	@Override
    public void setSpeed(float speed) {
		moveSpeed = speed;
	}

	public boolean isAutoPathingToEntity() {
		return autoPathToEntity;
	}

	@Override
    public Entity getTargetEntity() {
		return pathEndEntity;
	}

	@Override
    public Path getPathToXYZ(Vec3d pos, float targetRadius) {
		return createPath(theEntity, BlockPos.ofFloored(pos), targetRadius);
	}

	@Override
    public boolean startMovingTo(Vec3d pos, float targetRadius, float speed) {
		ticksStuck = 0;
		Path newPath = getPathToXYZ(pos, targetRadius);
		return newPath != null && setPath(newPath, speed);
	}

	@Override
    public Path getPathTowardsXZ(double x, double z, int min, int max, int verticalRange) {
		Vec3d target = findValidPointNear(x, z, min, max, verticalRange);
		return target == null ? null : getPathToXYZ(target, 0);
	}

	@Override
    public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed) {
		ticksStuck = 0;
		Path newPath = getPathTowardsXZ(MathHelper.floor(x), MathHelper.floor(z), min, max, verticalRange);
		return newPath != null && setPath(newPath, speed);
	}

	@Nullable
	@Override
    public Path getPathToEntity(Entity targetEntity, float targetRadius) {
		return createPath(theEntity, targetEntity.getBlockPos(), targetRadius);
	}

	@Override
    public boolean startMovingTo(Entity targetEntity, float targetRadius, float speed) {
		Path newPath = getPathToEntity(targetEntity, targetRadius);
		if (newPath == null) {
		    return false;
		}

		if (setPath(newPath, speed)) {
			pathEndEntity = targetEntity;
			return true;
		}

		pathEndEntity = null;
		return false;
	}

	@Override
    public void autoPathToEntity(Entity target) {
		autoPathToEntity = true;
		pathEndEntity = target;
	}

	@Override
    public boolean setPath(Path newPath, float speed) {
		if (newPath == null) {
			path = null;
			theEntity.onPathSet();
			return false;
		}

		moveSpeed = speed;
		lastDistance = getDistanceToActiveNode();
		ticksStuck = 0;
		resetStatus();

		entityCentre = theEntity.getBlockPos().toBottomCenterPos();

		path = newPath;
		activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());

		if (activeNode.action != PathAction.NONE) {
			nodeActionFinished = false;
		} else if (theEntity.getWidth() <= 1) {
			path.incrementPathIndex();
			if (!path.isFinished()) {
				activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
				if (activeNode.action != PathAction.NONE) {
					nodeActionFinished = false;
				}
			}
		} else {
			//UnstoppableN Custom Code
			//changed < to > this seems to have fixed some stuffs, not sure why
			while (theEntity.getPos().distanceTo(entityCentre.add(activeNode.pos.toCenterPos())) > theEntity.getWidth()) {
				path.incrementPathIndex();
				if (path.isFinished()) {
				    //System.out.println("Finished! : "+ path.getCurrentPathIndex()+" / "+ path.points.length);
				    break;
				}

				activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
				if (activeNode.action != PathAction.NONE) {
					nodeActionFinished = false;
				}
			}
		}

		if (noSunPathfind) {
			removeSunnyPath();
		}

		theEntity.onPathSet();
		return true;
	}

	@Override
    public Path getPath() {
		return path;
	}

	@Override
    public boolean isWaitingForTask() {
		return waitingForNotify;
	}

	@Override
    public void tick() {
	    if (theEntity.getTarget() != null) {
            transitionAIGoal(Goal.TARGET_ENTITY);
        } else if (theEntity.getNexus() != null) {
            transitionAIGoal(Goal.BREAK_NEXUS);
        } else {
            transitionAIGoal(Goal.CHILL);
        }

	    tickPathFinding();
	}

	private void tickPathFinding() {
		totalTicks++;
		if (autoPathToEntity) {
			updateAutoPathToEntity();
		}

		if (isIdle()) {
			noPathFollow();
			return;
		}

		if (isWaitingForTask()) {
			if (isMaintainingPos()) {
				theEntity.getMoveControl().moveTo(holdingPos.getX(), holdingPos.getY(), holdingPos.getZ(), moveSpeed);
			}
			return;
		}

		if (nodeActionFinished) {
			double distance = getDistanceToActiveNode();
			if (lastDistance - distance > 0.01D) {
				lastDistance = distance;
				ticksStuck--;
			} else {
				ticksStuck++;
			}

			int pathIndex = path.getCurrentPathIndex();
			pathFollow();
			if (isIdle()) {
				return;
			}
			if (path.getCurrentPathIndex() != pathIndex) {
				lastDistance = getDistanceToActiveNode();
				ticksStuck = 0;
				activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
				if (activeNode.action != PathAction.NONE) {
					nodeActionFinished = false;
				}
			}
		}

		if (nodeActionFinished) {
			if (!isPositionClearFrom(theEntity.getBlockPos(), activeNode.pos, theEntity)) {
				if (theEntity.onPathBlocked(path, this)) {
					setDoingTaskAndHoldOnPoint();
				}

			}

			if (!haltMovement) {
				if (pathEndEntity != null && pathEndEntity.getY() - theEntity.getY() <= 0 && theEntity.squaredDistanceTo(pathEndEntity) < 4.5D) {
					theEntity.getMoveControl().moveTo(
					        pathEndEntity.getX(),
					        pathEndEntity.getY(),
					        pathEndEntity.getZ(), moveSpeed);
				} else {
					theEntity.getMoveControl().moveTo(
					        activeNode.pos.getX() + entityCentre.x,
					        activeNode.pos.getY() + entityCentre.y,
					        activeNode.pos.getZ() + entityCentre.z, moveSpeed);
				}
			} else {
				haltMovement = false;
			}

		} else if (!handlePathAction()) {
			stop();
		}
	}

	@Override
    public void notifyTask(Status result) {
		waitingForNotify = false;
		lastActionResult = result;
	}

	@Override
    public Status getLastActionResult() {
		return lastActionResult;
	}

	@Override
    public boolean isIdle() {
		return path == null || path.isFinished();
	}

	@Override
    public int getStuckTime() {
		return ticksStuck;
	}

	@Override
    public float getLastPathDistanceToTarget() {
		if (isIdle()) {
			if (path != null && path.getIntendedTarget() != null) {
				PathNode node = path.getIntendedTarget();
				return MathHelper.sqrt((float) theEntity.getBlockPos().getSquaredDistance(node.pos));
			}
			return 0;
		}

		return path.getFinalPathPoint().distanceTo(path.getIntendedTarget());
	}

	@Override
    public void stop() {
		path = null;
		autoPathToEntity = false;
		resetStatus();
	}

	@Override
    public void haltForTick() {
		haltMovement = true;
	}

	protected Path createPath(EntityIMLiving entity, Entity target, float targetRadius) {
		return createPath(entity, target.getBlockPos(), targetRadius);
	}

	protected Path createPath(EntityIMLiving entity, BlockPos pos, float targetRadius) {
		actor.setCurrentTargetPos(pos);
		BlockView terrainCache = getChunkCache(entity.getBlockPos(), pos, 16);
		INexusAccess nexus = entity.getNexus();
		if (nexus != null) {
			terrainCache = nexus.getAttackerAI().wrapEntityData(terrainCache);
		}
		float maxSearchRange = 12 + MathHelper.sqrt((float) entity.getBlockPos().getSquaredDistance(pos));
		return pathSource.createPath(entity, pos, targetRadius, maxSearchRange, terrainCache);
	}

	protected void pathFollow() {
		Vec3d pos = getPos();
		int maxNextLegIndex = path.getCurrentPathIndex() - 1;

		PathNode nextPoint = path.getPathPointFromIndex(path.getCurrentPathIndex());
		if (nextPoint.pos.getY() == (int) pos.y && maxNextLegIndex < path.getCurrentPathLength() - 1) {
			maxNextLegIndex++;

			boolean canConsolidate = true;
			int prevIndex = maxNextLegIndex - 2;
			if (prevIndex >= 0 && path.getPathPointFromIndex(prevIndex).action != PathAction.NONE) {
				canConsolidate = false;
			}
			if (canConsolidate && actor.canStandAt(theEntity.getWorld(), theEntity.getBlockPos())) {
				while (maxNextLegIndex < path.getCurrentPathLength() - 1
				        && path.getPathPointFromIndex(maxNextLegIndex).pos.getY() == (int) pos.y
				        && path.getPathPointFromIndex(maxNextLegIndex).action == PathAction.NONE) {
					maxNextLegIndex++;
				}
			}

		}

		float reach = MathHelper.square(theEntity.getWidth() * 0.5F);
		for (int j = path.getCurrentPathIndex(); j <= maxNextLegIndex; j++) {
			if (pos.squaredDistanceTo(path.getPositionAtIndex(theEntity, j)) < reach) {
				path.setCurrentPathIndex(j + 1);
			}
		}

		Vec3i size = new Vec3i(
		        (int) Math.ceil(this.theEntity.getWidth()),
		        (int) this.theEntity.getHeight() + 1,
		        (int) Math.ceil(this.theEntity.getWidth())
        );

		while (maxNextLegIndex > path.getCurrentPathIndex() && !isDirectPathBetweenPoints(pos, path.getPositionAtIndex(theEntity, maxNextLegIndex), size)) {
		    maxNextLegIndex--;
		}

		for (int i = path.getCurrentPathIndex() + 1; i < maxNextLegIndex; i++) {
			if (path.getPathPointFromIndex(i).action != PathAction.NONE) {
			    maxNextLegIndex = i;
				break;
			}
		}

		if (path.getCurrentPathIndex() < maxNextLegIndex) {
			path.setCurrentPathIndex(maxNextLegIndex);
		}
	}

	protected void noPathFollow() {
	}

	protected void updateAutoPathToEntity() {
		if (pathEndEntity != null && (isIdle() || (pathEndEntity.getPos().distanceTo(pathEndEntityLastPos) / (6 + theEntity.getPos().distanceTo(pathEndEntityLastPos)) > 0.1D))) {
			Path newPath = getPathToEntity(pathEndEntity, 0);
			if (newPath != null && setPath(newPath, moveSpeed)) {
				pathEndEntityLastPos = pathEndEntity.getPos();
			}
		}
	}

	protected double getDistanceToActiveNode() {
		return activeNode == null ? 0 : activeNode.pos.toBottomCenterPos().subtract(theEntity.getPos()).length();
	}

	protected boolean handlePathAction() {
		this.nodeActionFinished = true;
		return true;
	}

	protected boolean setDoingTask() {
		waitingForNotify = true;
		actionCleared = false;
		return true;
	}

	protected boolean setDoingTaskAndHold() {
		waitingForNotify = true;
		actionCleared = false;
		setMaintainPosOnWait(theEntity.getPos());
		theEntity.setIsHoldingIntoLadder(true);
		return true;
	}

	protected boolean setDoingTaskAndHoldOnPoint() {
		waitingForNotify = true;
		actionCleared = false;
		setMaintainPosOnWait(activeNode.pos.toBottomCenterPos());
		theEntity.setIsHoldingIntoLadder(true);
		return true;
	}

	protected void resetStatus() {
		setNoMaintainPos();
		theEntity.setIsHoldingIntoLadder(false);
		nodeActionFinished = true;
		actionCleared = true;
		waitingForNotify = false;
	}

	@Override
    public Vec3d getPos() {
		return new Vec3d(theEntity.getX(), getPathableYPos(), theEntity.getZ());
	}

	protected EntityIMLiving getEntity() {
		return this.theEntity;
	}

	@SuppressWarnings("deprecation")
    private int getPathableYPos() {
		if (!theEntity.isTouchingWater() || !canSwim) {
			return theEntity.getBlockPos().getY();
		}

		final BlockPos.Mutable mutable = theEntity.getBlockPos().mutableCopy();
		final int initialY = mutable.getY();
		int y = initialY - 1;
		BlockState state;
		int steps = 0;

		do {
		    state = theEntity.getWorld().getBlockState(mutable.setY(++y));
			if (++steps > 16) {
				return initialY;
			}
		} while (state.isLiquid() && y < theEntity.getWorld().getTopY());

		return y;
	}

	@Nullable
	protected Vec3d findValidPointNear(double x, double z, int min, int max, int verticalRange) {
		double xOffset = x - theEntity.getX();
		double zOffset = z - theEntity.getZ();
		double h = Math.sqrt(xOffset * xOffset + zOffset * zOffset);

		if (h < 0.5D) {
			return null;
		}

		double distance = min + theEntity.getWorld().getRandom().nextInt(max - min);
		int xi = MathHelper.floor(xOffset * (distance / h) + theEntity.getX());
		int zi = MathHelper.floor(zOffset * (distance / h) + theEntity.getZ());
		int y = MathHelper.floor(theEntity.getY());

		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int vertical = 0; vertical < verticalRange; vertical = vertical > 0 ? vertical * -1 : vertical * -1 + 1) {
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (actor.canStandAtAndIsValid(theEntity.getWorld(), mutable.set(xi + i, y + vertical, zi + j))) {
						return new Vec3d(xi + i, y + vertical, zi + j);
					}
				}
			}
		}

		return null;
	}

	protected void removeSunnyPath() {
		if (theEntity.getWorld().isSkyVisible(theEntity.getBlockPos())) {
			return;
		}

		for (int i = 0; i < path.getCurrentPathLength(); i++) {
			PathNode pathpoint = path.getPathPointFromIndex(i);

			if (theEntity.getWorld().isSkyVisible(pathpoint.pos)) {
				path.setCurrentPathLength(i - 1);
				return;
			}
		}
	}

	protected boolean isDirectPathBetweenPoints(Vec3d pos1, Vec3d pos2, Vec3i size) {
	    BlockPos.Mutable mutable = new BlockPos.Mutable().set(pos1.x, pos1.y, pos1.z);

		Vec3d delta = pos2.subtract(pos1);

		if (delta.horizontalLength() < 1.0E-008D) {
			return false;
		}

		delta = delta.multiply(1, 0, 1).normalize();

		if (!isSafeToStandAt(mutable, size.add(2, 0, 2), pos1, delta)) {
			return false;
		}

		double xIncrement = 1D / delta.x;
		double zIncrement = 1D / delta.z;
		double xOffset = mutable.getX() * (1 - pos1.x);
		double zOffset = mutable.getZ() * (1 - pos1.z);

		if (delta.x >= 0) {
			xOffset++;
		}

		if (delta.z >= 0) {
			zOffset++;
		}

		xOffset *= xIncrement;
		zOffset *= zIncrement;
		byte xDirection = (byte)Math.signum(delta.x);
		byte zDirection = (byte)Math.signum(delta.z);
		int x2 = MathHelper.floor(pos2.x);
		int z2 = MathHelper.floor(pos2.z);

		for (; (x2 - mutable.getX()) * xDirection > 0 || (z2 - mutable.getZ()) * zDirection > 0;) {
			if (xOffset < zOffset) {
				xOffset += xIncrement;
				mutable.move(xDirection, 0, 0);
			} else {
				zOffset += zIncrement;
				mutable.move(0, 0, zDirection);
			}

			if (!isSafeToStandAt(mutable, size, pos1, delta)) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("deprecation")
    protected boolean isSafeToStandAt(BlockPos.Mutable pos, Vec3i size, Vec3d entityPosition, Vec3d delta) {
	    pos.move(-size.getX() / 2, 0, -size.getZ() / 2);

		if (!isPositionClear(pos, size, entityPosition, delta.x, delta.z)) {
			return false;
		}

		for (BlockPos p : BlockPos.iterate(pos, pos.add(size.getX(), 0, size.getZ()))) {
		    Vec3d centerP = p.toBottomCenterPos().subtract(entityPosition);

            if (centerP.x * delta.x + centerP.z * delta.z >= 0) {
                BlockState block = theEntity.getWorld().getBlockState(p.down());
                if (block.isAir()
                    || block.isLiquid() && ((block.getFluidState().isIn(FluidTags.WATER) && !theEntity.isSubmergedInWater()) || block.getFluidState().isIn(FluidTags.LAVA))
                    || !block.isSolidBlock(theEntity.getWorld(), p)) {
                    return false;
                }
            }
		}

		return true;
	}

	@SuppressWarnings("deprecation")
    protected boolean isPositionClear(BlockPos pos, Vec3i size, Vec3d entityPostion, double vecX, double vecZ) {
	    for (BlockPos p : BlockPos.iterate(pos, pos.add(size))) {
	        double d = p.getX() + 0.5D - entityPostion.x;
            double d1 = p.getZ() + 0.5D - entityPostion.z;

            if (d * vecX + d1 * vecZ >= 0) {
                BlockState block = theEntity.getWorld().getBlockState(p);

                if (!block.isAir() && block.blocksMovement()) {
                    return false;
                }
            }
	    }
		return true;
	}

	@SuppressWarnings("deprecation")
	protected boolean isPositionClearFrom(BlockPos from, BlockPos to, EntityIMLiving entity) {
		if (to.getY() > from.getY()) {
			BlockState block = theEntity.getWorld().getBlockState(from.add(0, MathHelper.ceil(entity.getHeight()), 0));
			if (!block.isAir() && block.blocksMovement()) {
				return false;
			}
		}

		return isPositionClear(to, entity);
	}

	@SuppressWarnings("deprecation")
	protected boolean isPositionClear(BlockPos pos, EntityIMLiving entity) {
	    return BlockPos.stream(entity.getDimensions(entity.getPose()).getBoxAt(pos.toBottomCenterPos())).allMatch(p -> {
	        BlockState block = theEntity.getWorld().getBlockState(p);
	        return block.isAir() || !block.blocksMovement();
	    });
	}

	protected ChunkCache getChunkCache(BlockPos p1, BlockPos p2, float axisExpand) {
        BlockBox box = BlockBox.create(p1, p2).expand((int) axisExpand);
        return new ChunkCache(theEntity.getWorld(),
                new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ()),
                new BlockPos(box.getMaxX(), box.getMaxY(), box.getMaxZ())
        );
	}
}
