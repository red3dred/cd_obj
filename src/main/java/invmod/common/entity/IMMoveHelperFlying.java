package invmod.common.entity;

import invmod.common.util.MathUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class IMMoveHelperFlying extends IMMoveHelper {
	private EntityIMFlying entity;
	private double targetFlySpeed;
	private boolean wantsToBeFlying;

	public IMMoveHelperFlying(EntityIMFlying entity) {
		super(entity);
		this.entity = entity;
		this.wantsToBeFlying = false;
	}

	public void setHeading(float yaw, float pitch, float idealSpeed, int time) {
		double x = entity.getX() + Math.sin(yaw * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		double y = entity.getY() + Math.sin(pitch * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		double z = entity.getZ() + Math.cos(yaw * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		moveTo(x, y, z, idealSpeed);
	}

	public void setWantsToBeFlying(boolean flag) {
		this.wantsToBeFlying = flag;
	}

	@Override
    public void tick() {
		entity.setForwardSpeed(0);
		entity.setAcceleration(Vec3d.ZERO);
		if ((!needsUpdate) && (entity.getMoveState() != MoveState.FLYING)) {
			entity.setMoveState(MoveState.STANDING);
			entity.setFlyState(FlyState.GROUNDED);
			entity.setPitch(correctRotation(entity.getPitch(), 50, 4));
			return;
		}
		needsUpdate = false;

		if (wantsToBeFlying) {
			if (entity.getFlyState() == FlyState.GROUNDED) {
				entity.setMoveState(MoveState.RUNNING);
				entity.setFlyState(FlyState.TAKEOFF);
			} else if (entity.getFlyState() == FlyState.FLYING) {
				entity.setMoveState(MoveState.FLYING);
			}

		} else if (entity.getFlyState() == FlyState.FLYING) {
			entity.setFlyState(FlyState.LANDING);
		}

		if (entity.getFlyState() == FlyState.FLYING) {
			FlyState result = doFlying();
			if (result == FlyState.GROUNDED)
				entity.setMoveState(MoveState.STANDING);
			else if (result == FlyState.FLYING) {
				entity.setMoveState(MoveState.FLYING);
			}
			entity.setFlyState(result);
		} else if (entity.getFlyState() == FlyState.TAKEOFF) {
			FlyState result = doTakeOff();
			if (result == FlyState.GROUNDED)
				entity.setMoveState(MoveState.STANDING);
			else if (result == FlyState.TAKEOFF)
				entity.setMoveState(MoveState.RUNNING);
			else if (result == FlyState.FLYING) {
				entity.setMoveState(MoveState.FLYING);
			}
			entity.setFlyState(result);
		} else if (entity.getFlyState() == FlyState.LANDING || entity.getFlyState() == FlyState.TOUCHDOWN) {
			FlyState result = doLanding();
			if (result == FlyState.GROUNDED || result == FlyState.TOUCHDOWN) {
				entity.setMoveState(MoveState.RUNNING);
			}
			entity.setFlyState(result);
		} else {
			MoveState result = doGroundMovement();
			entity.setMoveState(result);
		}
	}

	@Override
    protected MoveState doGroundMovement() {
		entity.setGroundFriction(0);
		entity.setRotationRoll(correctRotation(entity.getRotationRoll(), 0, 6));
		targetSpeed = entity.getMovementSpeed();
		entity.setPitch(correctRotation(entity.getPitch(), 50, 4));
		return super.doGroundMovement();
	}

	protected FlyState doFlying() {
		this.targetFlySpeed = this.speed;
		return fly();
	}

	protected FlyState fly() {
		entity.setGroundFriction(1.0F);
		Vec3d delta = new Vec3d(targetX, targetY, targetZ).subtract(entity.getPos());

		double dXZSq = delta.horizontalLengthSquared();
		double distanceSquared = dXZSq + MathHelper.square(delta.y);

		if (distanceSquared > 0.04D) {
			int timeToTurn = 10;
			float gravity = (float)entity.getGravity();
			float liftConstant = gravity;
			Vec3d acelleration = Vec3d.ZERO;
			Vec3d velocity = entity.getVelocity();
			double hSpeedSq = velocity.horizontalLengthSquared();
			if (hSpeedSq == 0) {
				hSpeedSq = 1.0E-008D;
			}
			double horizontalSpeed = Math.sqrt(hSpeedSq);
			double flySpeed = Math.sqrt(hSpeedSq + MathHelper.square(velocity.y));

			double desiredYVelocity = delta.y / timeToTurn;
			double dVelY = desiredYVelocity - (velocity.y - gravity);

			float minFlightSpeed = 0.05F;
			if (flySpeed < minFlightSpeed) {
				entity.setYaw(correctRotation(entity.getYaw(), (float) (Math.atan2(delta.z, delta.x) * MathHelper.DEGREES_PER_RADIAN - 90), entity.getTurnRate()));
				if (entity.isOnGround()) {
					return FlyState.GROUNDED;
				}
			} else {
				double liftForce = flySpeed / (entity.getMaxPoweredFlightSpeed() * entity.getLiftFactor()) * liftConstant;
				double climbForce = liftForce * horizontalSpeed / (Math.abs(velocity.y) + horizontalSpeed);
				double forwardForce = liftForce * Math.abs(velocity.y) / (Math.abs(velocity.y) + horizontalSpeed);
				double turnForce = liftForce;
				double climbAccel;
				if (dVelY < 0.0D) {
					double maxDiveForce = entity.getMaxTurnForce() - gravity;
					climbAccel = -Math.min(Math.min(climbForce, maxDiveForce), -dVelY);
				} else {
					double maxClimbForce = entity.getMaxTurnForce() + gravity;
					climbAccel = Math.min(Math.min(climbForce, maxClimbForce), dVelY);
				}

				float minBankForce = 0.01F;
				if (turnForce < minBankForce) {
					turnForce = minBankForce;
				}

				double desiredXZHeading = Math.atan2(delta.z, delta.x) - 1.570796326794897D;
				double currXZHeading = Math.atan2(velocity.z, velocity.x) - 1.570796326794897D;
				double dXZHeading = MathUtil.boundAnglePiRad(desiredXZHeading - currXZHeading);
				double bankForce = horizontalSpeed * dXZHeading / timeToTurn;
				double maxBankForce = Math.min(turnForce, entity.getMaxTurnForce());
				if (bankForce > maxBankForce)
					bankForce = maxBankForce;
				else if (bankForce < -maxBankForce) {
					bankForce = -maxBankForce;
				}

				double bankXAccel = bankForce * -velocity.z / horizontalSpeed;
				double bankZAccel = bankForce * velocity.x / horizontalSpeed;

				acelleration = acelleration.add(bankXAccel, climbAccel, bankZAccel);
				velocity = velocity.add(bankXAccel, climbAccel, bankZAccel);

				double middlePitch = 15.0D;
				double newPitch;
				if (velocity.y - gravity < 0) {
					double climbForceRatio = acelleration.y / climbForce;
					if (climbForceRatio > 1.0D)
						climbForceRatio = 1.0D;
					else if (climbForceRatio < -1.0D) {
						climbForceRatio = -1.0D;
					}
					double xzSpeed = velocity.horizontalLength();
					double velPitch = xzSpeed > 0 ? Math.atan(velocity.y / xzSpeed) * MathHelper.DEGREES_PER_RADIAN : -180;
					double pitchInfluence = Math.max(0, (entity.getMaxPoweredFlightSpeed() - Math.abs(velocity.y)) / entity.getMaxPoweredFlightSpeed());
					newPitch = velPitch + 15 * climbForceRatio * pitchInfluence;
				} else {
					double pitchLimit = entity.getMaxPitch();
					double climbForceRatio = Math.min(acelleration.y / climbForce, 1.0D);
					newPitch = middlePitch + (pitchLimit - middlePitch) * climbForceRatio;
				}
				newPitch = correctRotation(entity.getPitch(), (float) newPitch, 1.5F);
				double newYaw = Math.atan2(velocity.z, velocity.x) * MathHelper.DEGREES_PER_RADIAN - 90;
				newYaw = correctRotation(entity.getYaw(), (float) newYaw, entity.getTurnRate());
				entity.updatePositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), (float) newYaw, (float) newPitch);
				double newRoll = 60 * bankForce / turnForce;
				entity.setRotationRoll(correctRotation(entity.getRotationRoll(), (float) newRoll, 6));
				double horizontalForce = velocity.y > 0 ? -climbAccel : forwardForce;
				int xDirection = velocity.x > 0 ? 1 : -1;
				int zDirection = velocity.z > 0 ? 1 : -1;
				double hComponentX = xDirection * velocity.x / (xDirection * velocity.x + zDirection * velocity.z);

				double xLiftAccel = xDirection * horizontalForce * hComponentX;
				double zLiftAccel = zDirection * horizontalForce * (1 - hComponentX);

				double loss = 0.4D;
				xLiftAccel += xDirection * -Math.abs(bankForce * loss) * hComponentX;
				zLiftAccel += zDirection * -Math.abs(bankForce * loss) * (1 - hComponentX);

				acelleration = acelleration.add(xLiftAccel, 0, zLiftAccel);
			}

			if (flySpeed < this.targetFlySpeed) {
				entity.setThrustEffort(0.6F);
				if (!entity.isThrustOn()) {
					entity.setThrustOn(true);
				}
				acelleration = acelleration.add(calcThrust((dVelY - acelleration.y) / entity.getThrust()));
			} else if (flySpeed > this.targetFlySpeed * 1.8D) {
				entity.setThrustEffort(1.0F);
				if (!entity.isThrustOn()) {
					entity.setThrustOn(true);
				}
				acelleration = acelleration.add(calcThrust((dVelY - acelleration.y) / (entity.getThrust() * 10.0F)).multiply(-1, 1, -1));
			} else if (entity.isThrustOn()) {
				entity.setThrustOn(false);
			}

			entity.setAcceleration(acelleration);
		}
		return FlyState.FLYING;
	}

	protected FlyState doTakeOff() {
		entity.setGroundFriction(0.98F);
		entity.setThrustOn(true);
		entity.setThrustEffort(1);
		targetSpeed = entity.getMovementSpeed();

		MoveState result = doGroundMovement();
		if (result == MoveState.STANDING) {
			return FlyState.GROUNDED;
		}
		if (entity.horizontalCollision) {
			entity.getJumpControl().setActive();
		}
		entity.setAcceleration(calcThrust(0));
		double speed = entity.getVelocity().length();

		entity.setPitch(correctRotation(entity.getPitch(), 40, 4));

		float gravity = (float)entity.getGravity();
		float liftConstant = gravity;
		double liftForce = speed / (entity.getMaxPoweredFlightSpeed() * entity.getLiftFactor()) * liftConstant;

		return liftForce > gravity ? FlyState.FLYING : FlyState.TAKEOFF;
	}

	protected FlyState doLanding() {
		entity.setGroundFriction(0.3F);
		BlockPos.Mutable mutable = entity.getBlockPos().mutableCopy();

		for (int i = 1; i < 5; i++) {
			if (!entity.getWorld().isAir(mutable.move(Direction.DOWN))) {
				break;
			}
			targetFlySpeed = (speed * (0.66F - (0.4F - (i - 1) * 0.133F)));
		}

		FlyState result = fly();
		entity.setThrustOn(true);
		if (result == FlyState.FLYING && entity.isOnGround()) {
			if (entity.getVelocity().length() < entity.getLandingSpeedThreshold()) {
				return FlyState.GROUNDED;
			}

			entity.setRotationRoll(correctRotation(entity.getRotationRoll(), 40, 6));
			return FlyState.TOUCHDOWN;
		}

		return FlyState.LANDING;
	}

	protected Vec3d calcThrust(double desiredVThrustRatio) {
		double vThrustRatio = MathHelper.clamp(desiredVThrustRatio, entity.getThrustComponentRatioMin(), entity.getThrustComponentRatioMax());
		double hThrust = (1 - vThrustRatio) * entity.getThrust();
		return new Vec3d(
		        hThrust * -Math.sin(entity.getYaw() * MathHelper.RADIANS_PER_DEGREE),
		        vThrustRatio * entity.getThrust(),
		        hThrust * Math.cos(entity.getYaw() * MathHelper.RADIANS_PER_DEGREE)
        );
	}
}