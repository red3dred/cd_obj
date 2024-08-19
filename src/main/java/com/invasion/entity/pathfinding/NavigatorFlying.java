package com.invasion.entity.pathfinding;


import org.joml.Vector3f;

import com.invasion.IBlockAccessExtended;
import com.invasion.block.BlockMetadata;
import com.invasion.block.DestructableType;
import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.ai.MoveState;
import com.invasion.util.math.CoordsInt;
import com.invasion.util.math.MathUtil;

import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

public class NavigatorFlying extends NavigatorIM implements INavigationFlying {
	private static final int VISION_RESOLUTION_H = 30;
	private static final int VISION_RESOLUTION_V = 20;

	private final EntityIMFlying theEntity;
	private INavigationFlying.MoveType moveType = INavigationFlying.MoveType.MIXED;
	private boolean wantsToBeFlying;
	private float targetYaw;
	private float targetPitch;
	private float targetSpeed;
	private float visionDistance = 14;
	private int visionUpdateRate = 3;
	private int timeSinceVision = 3;
	private float[][] retina = new float[VISION_RESOLUTION_H][VISION_RESOLUTION_V];
	private float[][] headingAppeal = new float[28][18];

	private Vec3d finalTarget;
	private boolean isCircling;
	private float circlingHeight;
	private float circlingRadius;
	private float pitchBias;
	private float pitchBiasAmount;
	private int timeLookingForEntity;
	private boolean precisionTarget;
	private float closestDistToTarget;
	private int timeSinceGotCloser;

	public NavigatorFlying(EntityIMFlying entityFlying, IPathSource pathSource) {
		super(entityFlying, pathSource);
		theEntity = entityFlying;
		targetYaw = entityFlying.getYaw();
		targetSpeed = entityFlying.getMaxPoweredFlightSpeed();
	}

    @Override
    protected <T extends Entity> Actor<T> createActor(T entity) {
        return new Actor<>(entity) {

            @Override
            public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
                if (!theEntity.getPathFindFlying()) {
                    super.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
                    return;
                }

                if (terrainMap.isOutOfHeightLimit(currentNode.pos)) {
                    return;
                }

                BlockPos.Mutable mutable = currentNode.pos.mutableCopy();

                if (getNodeDestructability(terrainMap, mutable.move(Direction.UP)) > DestructableType.UNBREAKABLE) {
                    pathFinder.addNode(mutable.toImmutable(), PathAction.NONE);
                }

                if (getNodeDestructability(terrainMap, mutable.move(Direction.DOWN, 2)) > DestructableType.UNBREAKABLE) {
                    pathFinder.addNode(mutable.toImmutable(), PathAction.NONE);
                }

                for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
                    if (getNodeDestructability(terrainMap, mutable.set(currentNode.pos).move(offset)) > DestructableType.UNBREAKABLE) {
                        pathFinder.addNode(mutable.toImmutable(), PathAction.NONE);
                    }
                }

                if (canSwimHorizontal()) {
                    for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
                        if (getNodeDestructability(terrainMap, mutable.set(currentNode.pos).move(offset)) == DestructableType.FLUID) {
                            pathFinder.addNode(mutable.toImmutable(), PathAction.SWIM);
                        }
                    }
                }
            }

            @Override
            public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
                float multiplier = 1 + (IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3;

                BlockPos.Mutable mutable = node.pos.mutableCopy();

                for (int i = -1; i > -6; i--) {
                    BlockState state = terrainMap.getBlockState(mutable.set(node.pos).move(Direction.UP, i));
                    if (!state.isAir()) {
                        int blockType = BlockMetadata.getBlockType(state);
                        if (blockType != 1) {
                            multiplier += 1 + i * 0.2F;
                            if (blockType != 2 || i < -2) {
                                break;
                            }
                            multiplier += 6 + i * 2;
                            break;
                        }
                    }
                }

                for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
                    for (int j = 1; j <= 2; j++) {
                        int blockType = BlockMetadata.getBlockType(terrainMap.getBlockState(mutable.set(node.pos).move(offset, j)));
                        if (blockType != 1) {
                            multiplier += 1.5F - j * 0.5F;
                            if (blockType != 2) {
                                break;
                            }
                            multiplier += 6 - j * 2;
                            break;
                        }

                    }

                }

                if (node.action == PathAction.SWIM) {
                    multiplier *= (node.pos.getY() <= prevNode.pos.getY() && !terrainMap.getBlockState(node.pos.up()).isAir() ? 3 : 1);
                    return prevNode.distanceTo(node) * 1.3F * multiplier;
                }

                BlockState state = terrainMap.getBlockState(node.pos);
                return prevNode.distanceTo(node) * BlockMetadata.getCost(state).orElse(state.isSolidBlock(terrainMap, node.pos) ? 3.2F : 1) * multiplier;
            }
        };
    }

	@Override
    public void setMovementType(INavigationFlying.MoveType moveType) {
		this.moveType = moveType;
	}

	@Override
    public void enableDirectTarget(boolean enabled) {
		precisionTarget = enabled;
	}

	@Override
    public void setLandingPath() {
		clearPath();
		setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
		setWantsToBeFlying(false);
	}

	@Override
    public void setCirclingPath(Vec3d pos, float preferredHeight, float preferredRadius) {
		clearPath();
		this.finalTarget = pos;
		this.circlingHeight = preferredHeight;
		this.circlingRadius = preferredRadius;
		this.isCircling = true;
	}

	@Override
    public float getDistanceToCirclingRadius() {
		return finalTarget == null ? Float.MAX_VALUE : (float)theEntity.getPos().distanceTo(finalTarget) - circlingRadius;
	}

	@Override
    public void setFlySpeed(float speed) {
		targetSpeed = speed;
	}

	@Override
    public void setPitchBias(float pitch, float biasAmount) {
		pitchBias = pitch;
		pitchBiasAmount = biasAmount;
	}

	@Override
    protected void updateAutoPathToEntity() {
		double dist = theEntity.distanceTo(pathEndEntity);
		if (dist < closestDistToTarget - 1) {
			closestDistToTarget = ((float) dist);
			timeSinceGotCloser = 0;
		} else {
			timeSinceGotCloser++;
		}

		boolean pathUpdate = false;
		boolean needsPathfinder = false;
		if (path != null) {
			double dSq = MathHelper.square(dist);
			if ((moveType == INavigationFlying.MoveType.PREFER_FLYING || (moveType == INavigationFlying.MoveType.MIXED && dSq > 100)) && theEntity.canSee(pathEndEntity)) {
				this.timeLookingForEntity = 0;
				pathUpdate = true;
			} else {
				double d1 = Math.sqrt(pathEndEntity.squaredDistanceTo(pathEndEntityLastPos));
				double d2 = Math.sqrt(theEntity.squaredDistanceTo(pathEndEntityLastPos));
				if (d1 / d2 > 0.1D) {
					pathUpdate = true;
				}
			}

		} else if (moveType == INavigationFlying.MoveType.PREFER_WALKING || timeSinceGotCloser > 160 || timeLookingForEntity > 600) {
			pathUpdate = true;
			needsPathfinder = true;
			timeSinceGotCloser = 0;
			timeLookingForEntity = 500;
		} else if (moveType == INavigationFlying.MoveType.MIXED) {
			double dSq = theEntity.squaredDistanceTo(pathEndEntity.getPos());
			if (dSq < 100) {
				pathUpdate = true;
			}

		}

		if (pathUpdate) {
			if (moveType == INavigationFlying.MoveType.PREFER_FLYING) {
				if (needsPathfinder) {
					theEntity.setPathfindFlying(true);
					path = createPath(theEntity, pathEndEntity, 0);
					if (path != null) {
						setWantsToBeFlying(true);
						setPath(path, moveSpeed);
					}

				} else {
					setWantsToBeFlying(true);
					resetStatus();
				}
			} else if (moveType == INavigationFlying.MoveType.MIXED) {
				theEntity.setPathfindFlying(false);
				Path path = createPath(theEntity, pathEndEntity, 0);
				if ((path != null) && (path.getCurrentPathLength() < dist * 1.8D)) {
					setWantsToBeFlying(false);
					setPath(path, moveSpeed);
				} else if (needsPathfinder) {
					theEntity.setPathfindFlying(true);
					path = createPath(theEntity, pathEndEntity, 0);
					setWantsToBeFlying(true);
					if (path != null) {
						setPath(path, moveSpeed);
					} else {
						resetStatus();
					}
				} else {
					setWantsToBeFlying(true);
					resetStatus();
				}
			} else {
				setWantsToBeFlying(false);
				theEntity.setPathfindFlying(false);
				Path path = createPath(theEntity, pathEndEntity, 0);
				if (path != null) {
					setPath(path, moveSpeed);
				}
			}
			pathEndEntityLastPos = pathEndEntity.getPos();
		}
	}

	@Override
    public void autoPathToEntity(Entity target) {
		super.autoPathToEntity(target);
		this.isCircling = false;
	}

	@Override
    public boolean tryMoveToEntity(Entity targetEntity, float targetRadius, float speed) {
		if (moveType != INavigationFlying.MoveType.PREFER_WALKING) {
			clearPath();
			pathEndEntity = targetEntity;
			finalTarget = pathEndEntity.getPos();
			isCircling = false;
			return true;
		}

		theEntity.setPathfindFlying(false);
		return super.tryMoveToEntity(targetEntity, targetRadius, speed);
	}

	@Override
    public boolean tryMoveToXYZ(Vec3d pos, float targetRadius, float speed) {
		if (moveType != INavigationFlying.MoveType.PREFER_WALKING) {
			clearPath();
			finalTarget = pos;
			isCircling = false;
			return true;
		}

		theEntity.setPathfindFlying(false);
		return super.tryMoveToXYZ(pos, targetRadius, speed);
	}

	@Override
    public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed) {
		Vec3d target = findValidPointNear(x, z, min, max, verticalRange);
		return target != null && tryMoveToXYZ(target, 0, speed);
	}

	@Override
    public void clearPath() {
		super.clearPath();
		pathEndEntity = null;
		isCircling = false;
	}

	@Override
    public boolean isCircling() {
		return isCircling;
	}

	@Override
    protected void pathFollow() {
		Vec3d vec3d = getEntityPosition();
		int maxNextLeg = path.getCurrentPathLength();

		float fa = MathHelper.square(theEntity.getWidth() * 0.5F);
		for (int j = path.getCurrentPathIndex(); j < maxNextLeg; j++) {
			if (vec3d.squaredDistanceTo(path.getPositionAtIndex(theEntity, j)) < fa) {
				path.setCurrentPathIndex(j + 1);
			}
		}
	}

	@Override
    protected void noPathFollow() {
		if (theEntity.getMoveState() != MoveState.FLYING && theEntity.getAIGoal() == Goal.CHILL) {
			setWantsToBeFlying(false);
			return;
		}

		if (moveType == INavigationFlying.MoveType.PREFER_FLYING)
			setWantsToBeFlying(true);
		else if (moveType == INavigationFlying.MoveType.PREFER_WALKING) {
			setWantsToBeFlying(false);
		}
		if (++timeSinceVision >= visionUpdateRate) {
			timeSinceVision = 0;
			if (!precisionTarget || pathEndEntity == null) {
				updateHeading();
			} else {
				updateHeadingDirectTarget(pathEndEntity);
			}
			theEntity.setTargetPos(convertToVector(targetYaw, targetPitch, targetSpeed).toVector3f());
		}
		Vector3f target = theEntity.getTargetPos();
		this.theEntity.getMoveControl().moveTo(target.x, target.y, target.z, targetSpeed);
	}

	protected Vec3d convertToVector(float yaw, float pitch, float idealSpeed) {
		int time = visionUpdateRate + 20;
		double x = theEntity.getX() + -Math.sin(yaw * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		double y = theEntity.getY() + Math.sin(pitch * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		double z = theEntity.getZ() + Math.cos(yaw * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		return new Vec3d(x, y, z);
	}

	protected void updateHeading() {
		float pixelDegreeH = 10;
		float pixelDegreeV = 11;
		for (int i = 0; i < VISION_RESOLUTION_H; i++) {
			double nextAngleH = i * pixelDegreeH + 0.5D * pixelDegreeH - 150 + theEntity.getYaw();
			for (int j = 0; j < 20; j++) {
				double nextAngleV = j * pixelDegreeV + 0.5D * pixelDegreeV - 110;
				double y = theEntity.getY() + Math.sin(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * visionDistance;
				double distanceXZ = Math.cos(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * visionDistance;
				double x = theEntity.getX() + -Math.sin(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
				double z = theEntity.getZ() + Math.cos(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;

				BlockHitResult object = this.theEntity.getWorld().raycast(new RaycastContext(theEntity.getPos().add(0, 1, 0), new Vec3d(x, y, z), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, theEntity));
				if ((object != null) && object.getType() == Type.BLOCK) {
					this.retina[i][j] = (float)object.getPos().distanceTo(theEntity.getPos());
				} else {
					this.retina[i][j] = (this.visionDistance + 1);
				}
			}

		}

		for (int i = 1; i < 29; i++) {
			for (int j = 1; j < 19; j++) {
				float appeal = retina[i][j];
				appeal += retina[(i - 1)][(j - 1)];
				appeal += retina[(i - 1)][j];
				appeal += retina[(i - 1)][(j + 1)];
				appeal += retina[i][(j - 1)];
				appeal += retina[i][(j + 1)];
				appeal += retina[(i + 1)][(j - 1)];
				appeal += retina[(i + 1)][j];
				appeal += retina[(i + 1)][(j + 1)];
				appeal /= 9;
				headingAppeal[(i - 1)][(j - 1)] = appeal;
			}

		}

		if (this.isCircling) {
			double dX = finalTarget.x - theEntity.getX();
			double dY = finalTarget.y - theEntity.getY();
			double dZ = finalTarget.z - theEntity.getZ();
			double dXZ = Math.sqrt(dX * dX + dZ * dZ);

			if ((dXZ > 0) && (dXZ > this.circlingRadius * 0.6D)) {
				double intersectRadius = Math.abs((circlingRadius - dXZ) * 2) + 8;
				if (intersectRadius > this.circlingRadius * 1.8D) {
					intersectRadius = dXZ + 5;
				}

				float preferredYaw1 = (float) (Math.acos((dXZ * dXZ - circlingRadius * circlingRadius + intersectRadius * intersectRadius) / (2 * dXZ) / intersectRadius) * MathHelper.DEGREES_PER_RADIAN);
				float preferredYaw2 = -preferredYaw1;

				double dYaw = Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90;
				preferredYaw1 = (float) (preferredYaw1 + dYaw);
				preferredYaw2 = (float) (preferredYaw2 + dYaw);

				float preferredPitch = (float) (Math.atan((dY + circlingHeight) / intersectRadius) * MathHelper.DEGREES_PER_RADIAN);

				float yawBias = (float) (1.5D * Math.abs(dXZ - circlingRadius) / circlingRadius);
				float pitchBias = (float) (1.9D * Math.abs((dY + circlingHeight) / circlingHeight));

				doHeadingBiasPass(this.headingAppeal, preferredYaw1, preferredYaw2, preferredPitch, yawBias, pitchBias);
			} else {
				float yawToTarget = (float) (Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90);
				yawToTarget += 180;
				float preferredPitch = (float) (Math.atan((dY + circlingHeight) / Math.abs(circlingRadius - dXZ)) * MathHelper.DEGREES_PER_RADIAN);
				float yawBias = (float) (0.5D * Math.abs(dXZ - circlingRadius) / circlingRadius);
				float pitchBias = (float) (0.9D * Math.abs((dY + circlingHeight) / circlingHeight));
				doHeadingBiasPass(this.headingAppeal, yawToTarget, yawToTarget, preferredPitch, yawBias, pitchBias);
			}
		} else if (pathEndEntity != null) {
			Vec2f angles = MathUtil.toPolar(pathEndEntity.getPos().subtract(theEntity.getPos()));
			doHeadingBiasPass(headingAppeal, angles.y, angles.y, angles.x, 20.6F, 20.6F);
		}

		if (this.pathEndEntity == null) {
			float dOldYaw = MathHelper.subtractAngles(targetYaw, theEntity.getYaw());
			float dOldPitch = targetPitch;
			float approxLastTargetX = MathHelper.clamp(dOldYaw / pixelDegreeH + 14, 0, 28);
			float approxLastTargetY = MathHelper.clamp(dOldPitch / pixelDegreeV + 9, 0, 18);
			float statusQuoBias = 0.4F;
			float falloffDist = 30;
			for (int i = 0; i < 28; i++) {
				float dXSq = (approxLastTargetX - i) * (approxLastTargetX - i);
				for (int j = 0; j < 18; j++) {
					float dY = approxLastTargetY - j;
					headingAppeal[i][j] *= (float)(1 + statusQuoBias - statusQuoBias * Math.sqrt(dXSq + dY * dY) / falloffDist);
				}
			}
		}

		if (pitchBias != 0) {
			doHeadingBiasPass(headingAppeal, 0, 0, pitchBias, 0, pitchBiasAmount);
		}

		if (!wantsToBeFlying) {
		    FloatFloatPair landingInfo = appraiseLanding();
			if (landingInfo.secondFloat() < 4) {
				if (landingInfo.firstFloat() >= 0.9F)
					doHeadingBiasPass(headingAppeal, 0, 0, -45, 0, 3.5F);
				else if (landingInfo.firstFloat() >= 0.65F) {
					doHeadingBiasPass(headingAppeal, 0, 0, -15, 0, 0.4F);
				}

			} else if (landingInfo.firstFloat() >= 0.52F) {
				doHeadingBiasPass(this.headingAppeal, 0, 0, -15, 0, 0.8F);
			}

		}

		IntIntPair bestPixel = chooseCoordinate();
		targetYaw = (theEntity.getYaw() - 150 + (bestPixel.firstInt() + 1) * pixelDegreeH + 0.5F * pixelDegreeH);
		targetPitch = (-110 + (bestPixel.secondInt() + 1) * pixelDegreeV + 0.5F * pixelDegreeV);
	}

	protected void updateHeadingDirectTarget(Entity target) {
	    Vec2f angles = MathUtil.toPolar(target.getPos().subtract(theEntity.getPos()));
		this.targetYaw = angles.x;
		this.targetPitch = angles.y;
	}

	protected IntIntPair chooseCoordinate() {
		int bestPixelX = 0;
		int bestPixelY = 0;
		for (int i = 0; i < 28; i++) {
			for (int j = 0; j < 18; j++) {
				if (headingAppeal[bestPixelX][bestPixelY] < headingAppeal[i][j]) {
					bestPixelX = i;
					bestPixelY = j;
				}
			}
		}
		return IntIntPair.of(bestPixelX, bestPixelY);
	}

	protected void setTarget(double x, double y, double z) {
		theEntity.setTargetPos(new Vec3d(x, y, z).toVector3f());
	}

	protected Vector3f getTarget() {
		return theEntity.getTargetPos();
	}

	protected void doHeadingBiasPass(float[][] array, float preferredYaw1, float preferredYaw2, float preferredPitch, float yawBias, float pitchBias) {
		float pixelDegreeH = 10;
		float pixelDegreeV = 11;
		for (int i = 0; i < array.length; i++) {
			double nextAngleH = (i + 1) * pixelDegreeH + 0.5D * pixelDegreeH - 150 + theEntity.getYaw();
			double dYaw1 = MathHelper.wrapDegrees(preferredYaw1 - nextAngleH);
			double dYaw2 = MathHelper.wrapDegrees(preferredYaw2 - nextAngleH);
			double yawBiasAmount = 1 + Math.min(Math.abs(dYaw1), Math.abs(dYaw2)) * yawBias / 180;
			for (int j = 0; j < array[0].length; j++) {
				double nextAngleV = (j + 1) * pixelDegreeV + 0.5D * pixelDegreeV - 110;
				double pitchBiasAmount = 1 + Math.abs(MathHelper.wrapDegrees(preferredPitch - nextAngleV)) * pitchBias / 180;
				array[i][j] /= yawBiasAmount * pitchBiasAmount;
			}
		}
	}

	private void setWantsToBeFlying(boolean flag) {
		wantsToBeFlying = flag;
		theEntity.getMoveControl().setWantsToBeFlying(flag);
	}

	private FloatFloatPair appraiseLanding() {
		float safety = 0;
		float distance = 0;
		int landingResolution = 3;
		double nextAngleH = theEntity.getYaw();
		for (int i = 0; i < landingResolution; i++) {
			double nextAngleV = -90 + i * VISION_RESOLUTION_H / landingResolution;
			double y = theEntity.getY() + Math.sin(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * 64;
			double distanceXZ = Math.cos(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * 64;
			double x = theEntity.getX() + -Math.sin(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
			double z = theEntity.getZ() + Math.cos(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
			Vec3d origin = theEntity.getPos();
			BlockHitResult hit = theEntity.getWorld().raycast(new RaycastContext(origin, new Vec3d(x, y, z), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, theEntity));
			if (hit != null && hit.getType() == Type.BLOCK) {
				BlockState Block = theEntity.getWorld().getBlockState(hit.getBlockPos());
				if (!actor.avoidsBlock(Block)) {
					safety += 0.7F;
				}
				if (hit.getSide() == Direction.UP) {
					safety += 0.3F;
				}
				distance = (float)hit.getPos().distanceTo(theEntity.getPos());
			} else {
				distance += 64;
			}
		}
		return FloatFloatPair.of(safety / landingResolution, distance / landingResolution);
	}
}
