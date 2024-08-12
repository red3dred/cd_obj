package invmod.common.entity;

import org.joml.Vector3f;

import invmod.common.IBlockAccessExtended;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.CoordsInt;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class EntityIMFlying extends EntityIMLiving {
    private static final TrackedData<Vector3f> TARGET_POS = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.VECTOR3F);
    private static final TrackedData<Boolean> THRUSTING = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> THRUST_EFFORT = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> FLY_STATE = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.INTEGER);

	private float liftFactor = 0.4F;
	private float maxPoweredFlightSpeed = 0.28F;
	private float thrust = 0.08F;
	private float thrustComponentRatioMin = 0;
	private float thrustComponentRatioMax = 0.1F;
	private float maxTurnForce = (float)(getGravity() * 3);

	private float optimalPitch = 52;
	private float maxRunSpeed = 0.45F;

	private Vec3d accelleration = Vec3d.ZERO;

	private boolean flyPathfind = true;
	private boolean debugFlying = true;

	public EntityIMFlying(EntityType<? extends EntityIMFlying> type, World world) {
		this(type, world, null);
	}

	public EntityIMFlying(EntityType<? extends EntityIMFlying> type, World world, INexusAccess nexus) {
		super(type, world, nexus);
		moveControl = new IMMoveHelperFlying(this);
		lookControl = new IMLookHelper(this);
	}

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TARGET_POS, new Vector3f());
        builder.add(THRUSTING, false);
        builder.add(THRUST_EFFORT, 1F);
        builder.add(FLY_STATE, FlyState.GROUNDED.ordinal());
    }

    public Vector3f getTargetPos() {
        return dataTracker.get(TARGET_POS);
    }

    public void setTargetPos(Vector3f target) {
        dataTracker.set(TARGET_POS, target);
    }

	@Override
    protected INavigation createIMNavigation(IPathSource pathSource) {
	    return new NavigatorFlying(this, pathSource);
	}

    @Override
    protected IPathSource createPathSource() {
        return new PathCreator(800, 200);
    }

    @Override
    protected BodyControl createBodyControl() {
        return new BodyControl(this) {
            @Override
            public void tick() {
            }
        };
    }

	@Override
	public void baseTick() {
		super.baseTick();
		/*if (!this.getWorld().isClient) {
			byte thrustData = this.dataWatcher.getWatchableObjectByte(META_THRUST_DATA);
			int oldThrustOn = thrustData & 0x1;
			int oldThrustEffortEncoded = thrustData >> 1 & 0xF;
			int thrustEffortEncoded = (int) (this.thrustEffort * 15.0F);
			if (this.thrustOn == oldThrustOn > 0) {
				if (thrustEffortEncoded == oldThrustEffortEncoded)
					;
		}*/
	}

	public FlyState getFlyState() {
		return FlyState.of(dataTracker.get(FLY_STATE));
	}

	public boolean isThrustOn() {
		return dataTracker.get(THRUSTING);
	}

	public float getThrustEffort() {
		return dataTracker.get(THRUST_EFFORT);
	}

    protected void setThrustEffort(float effortFactor) {
        dataTracker.set(THRUST_EFFORT, effortFactor);
    }

	@Deprecated
	public Vector3f getFlyTarget() {
		return getTargetPos();
	}

    @Override
    public boolean isClimbing() {
        return false;
    }

    public boolean hasFlyingDebug() {
        return this.debugFlying;
    }

    protected void setPathfindFlying(boolean flag) {
        this.flyPathfind = flag;
    }

    protected void setFlyState(FlyState flyState) {
        dataTracker.set(FLY_STATE, flyState.ordinal());
    }

    public float getMaxPoweredFlightSpeed() {
        return this.maxPoweredFlightSpeed;
    }

    protected float getLiftFactor() {
        return this.liftFactor;
    }

    protected void setLiftFactor(float liftFactor) {
        this.liftFactor = liftFactor;
    }

    protected float getThrust() {
        return this.thrust;
    }

    protected void setThrust(float thrust) {
        this.thrust = thrust;
    }

    protected float getThrustComponentRatioMin() {
        return this.thrustComponentRatioMin;
    }

    protected float getThrustComponentRatioMax() {
        return this.thrustComponentRatioMax;
    }

    protected float getMaxTurnForce() {
        return this.maxTurnForce;
    }

    protected float getMaxPitch() {
        return this.optimalPitch;
    }

    protected float getLandingSpeedThreshold() {
        return getMovementSpeed() * 1.2F;
    }

    protected float getMaxRunSpeed() {
        return this.maxRunSpeed;
    }

    protected void setFlightAccelerationVector(float xAccel, float yAccel, float zAccel) {
        setAcceleration(new Vec3d(xAccel, yAccel, zAccel));
    }

    protected void setAcceleration(Vec3d accelleration) {
       this.accelleration = accelleration;
    }

    protected void setThrustOn(boolean flag) {
        dataTracker.set(THRUSTING, flag);
    }

    protected void setMaxPoweredFlightSpeed(float speed) {
        this.maxPoweredFlightSpeed = speed;
        ((INavigationFlying)getNavigatorNew()).setFlySpeed(speed);
    }

    protected void setThrustComponentRatioMin(float ratio) {
        this.thrustComponentRatioMin = ratio;
    }

    protected void setThrustComponentRatioMax(float ratio) {
        this.thrustComponentRatioMax = ratio;
    }

    protected void setMaxTurnForce(float maxTurnForce) {
        this.maxTurnForce = maxTurnForce;
    }

    protected void setOptimalPitch(float pitch) {
        this.optimalPitch = pitch;
    }

    @Deprecated
    protected void setLandingSpeedThreshold(float speed) {
    }

    protected void setMaxRunSpeed(float speed) {
        this.maxRunSpeed = speed;
    }

	@Override
	public IMMoveHelperFlying getMoveControl() {
		return (IMMoveHelperFlying)super.getMoveControl();
	}

	@Override
	public IMLookHelper getLookControl() {
		return (IMLookHelper)super.getLookControl();
	}

	   // based on FlyingEntity (originals in comments)
	@Override
	public void travel(Vec3d movementInput) {
	    if (isLogicalSideForUpdatingMovement()) {
    		if (isTouchingWater()) {
    			updateVelocity(isAiDisabled() ? 0.02F : 0.04F /*0.02F*/, movementInput);
    			move(MovementType.SELF, getVelocity());
    			setVelocity(getVelocity().multiply(0.8F));
    		} else if (isInLava()) {
                updateVelocity(isAiDisabled() ? 0.02F : 0.04F /*0.02F*/, movementInput);
                move(MovementType.SELF, getVelocity());
                setVelocity(getVelocity().multiply(0.5));
    		} else {
    		    float groundFriction = 0.9995F;
                if (isOnGround()) {
                    groundFriction = getWorld().getBlockState(getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91F;
                }

                float landMoveSpeed = 0.16277137F / (groundFriction * groundFriction * groundFriction);
                groundFriction = getGroundFriction();
                if (isOnGround()) {
                    groundFriction = getWorld().getBlockState(getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91F;
                }

                /** IM additions **/
                // added air resistance
                float airResistance = getAirResistance();
                groundFriction *= getAirResistance();

                // added accelleration
                addVelocity(accelleration);
                /** end IM additions **/

    			updateVelocity(isOnGround() ? 0.1F * landMoveSpeed : 0.02F, movementInput);
    			move(MovementType.SELF, getVelocity());
    			//setVelocity(getVelocity().multiply(groundFriction));
    			setVelocity(getVelocity().multiply(groundFriction, airResistance, airResistance).add(-getGravity(), 0, 0));
    		}
	    }

	    updateLimbs(false);
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
	}

	@Override
	public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
		if (!flyPathfind) {
			super.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
		} else {
			calcPathOptionsFlying(terrainMap, currentNode, pathFinder);
		}
	}

	protected void calcPathOptionsFlying(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
		if (terrainMap.isOutOfHeightLimit(currentNode.pos)) {
			return;
		}

		BlockPos.Mutable mutable = currentNode.pos.mutableCopy();

		if (getCollide(terrainMap, mutable.move(Direction.UP)) > 0) {
			pathFinder.addNode(mutable.toImmutable(), PathAction.NONE);
		}

		if (getCollide(terrainMap, mutable.move(Direction.DOWN, 2)) > 0) {
			pathFinder.addNode(mutable.toImmutable(), PathAction.NONE);
		}

        for (int i = 0; i < 4; i++) {

            if (getCollide(terrainMap, mutable.set(currentNode.pos).move(CoordsInt.offsetAdjX[i], 0, CoordsInt.offsetAdjZ[i])) > 0) {
                pathFinder.addNode(mutable.toImmutable(), PathAction.NONE);
            }
        }
        if (canSwimHorizontal()) {
            for (int i = 0; i < 4; i++) {
                if (getCollide(terrainMap, mutable.set(currentNode.pos).move(CoordsInt.offsetAdjX[i], 0, CoordsInt.offsetAdjZ[i])) == -1)
                    pathFinder.addNode(mutable.toImmutable(), PathAction.SWIM);
            }
        }
	}

	@Override
	public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
		float multiplier = 1.0F + (IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3;

		BlockPos.Mutable mutable = node.pos.mutableCopy();

		for (int i = -1; i > -6; i--) {
			BlockState block = terrainMap.getBlockState(mutable.set(node.pos).move(Direction.UP, i));
			if (!block.isAir()) {
				int blockType = getBlockType(block.getBlock());
				if (blockType != 1) {
					multiplier += 1 + i * 0.2F;
					if (blockType != 2 || i < -2) {
						break;
					}
					multiplier += (6 + i * 2);
					break;
				}

			}

		}

		for (int i = 0; i < 4; i++) {
			for (int j = 1; j <= 2; j++) {
				BlockState block = terrainMap.getBlockState(mutable.set(node.pos).move(CoordsInt.offsetAdjX[i] * j, 0, CoordsInt.offsetAdjZ[i] * j));
				int blockType = getBlockType(block.getBlock());
				if (blockType != 1) {
					multiplier += 1.5F - j * 0.5F;
					if (blockType != 2 || i < -2) {
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

		BlockState block = terrainMap.getBlockState(node.pos);
		return prevNode.distanceTo(node) * EntityIMLiving.getBlockCost(block).orElse(block.isSolid() ? 3.2F : 1) * multiplier;
	}
}