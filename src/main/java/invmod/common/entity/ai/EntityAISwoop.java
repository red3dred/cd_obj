package invmod.common.entity.ai;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;
import invmod.common.entity.MoveState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class EntityAISwoop extends net.minecraft.entity.ai.goal.Goal {
    private static final int INITIAL_LINEUP_TIME = 25;

    private final EntityIMBird theEntity;

    private float minDiveClearanceY;

    @Nullable
    private LivingEntity swoopTarget;
    private float diveAngle;
    private float diveHeight;
    private float strikeDistance;
    private float minHeight = 6;
    private float minXZDistance = 10;
    private float maxSteepness = 40;
    private float finalRunLength = 4;
    private float finalRunArcLimit = 15;
    private int time;
    private boolean isCommittedToFinalRun;
    private boolean endSwoop;

    public EntityAISwoop(EntityIMBird entity) {
        theEntity = entity;
        strikeDistance = entity.getWidth() + 1.5F;
        setControls(EnumSet.of(Control.LOOK, Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (theEntity.getAIGoal() == Goal.FIND_ATTACK_OPPORTUNITY && theEntity.getTarget() != null) {
            swoopTarget = theEntity.getTarget();
            Vec3d delta = swoopTarget.getPos().subtract(theEntity.getPos());
            double dXZ = delta.horizontalLength();
            if (-delta.y < minHeight || dXZ < minXZDistance) {
                return false;
            }
            double pitchToTarget = Math.atan(delta.y / dXZ) * MathHelper.DEGREES_PER_RADIAN;
            if (pitchToTarget > maxSteepness) {
                return false;
            }
            finalRunLength = MathHelper.clamp((float) (dXZ * 0.42D), 4, 18);
            diveAngle = (float) Math.atan((dXZ - finalRunLength) / delta.y) * MathHelper.DEGREES_PER_RADIAN;
            if (swoopTarget != null && isSwoopPathClear(swoopTarget, diveAngle)) {
                diveHeight = (float) -delta.y;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return theEntity.getTarget() == swoopTarget && !endSwoop && theEntity.getMoveState() == MoveState.FLYING;
    }

    @Override
    public void start() {
        time = 0;
        theEntity.transitionAIGoal(Goal.SWOOP);
        ((INavigationFlying)theEntity.getNavigatorNew()).setMovementType(INavigationFlying.MoveType.PREFER_FLYING);
        theEntity.getNavigatorNew().tryMoveToEntity(swoopTarget, 0, theEntity.getMaxPoweredFlightSpeed());
        theEntity.doScreech();
    }

    @Override
    public void stop() {
        endSwoop = false;
        isCommittedToFinalRun = false;
        ((INavigationFlying)theEntity.getNavigatorNew()).enableDirectTarget(false);
        if (theEntity.getAIGoal() == Goal.SWOOP) {
            theEntity.transitionAIGoal(Goal.NONE);
            theEntity.setClawsForward(false);
        }
    }

    @Override
    public void tick() {
        time++;
        if (!isCommittedToFinalRun) {
            if (theEntity.distanceTo(swoopTarget) < finalRunLength) {
                ((INavigationFlying)theEntity.getNavigatorNew()).setPitchBias(0, 1);
                if (isFinalRunLinedUp()) {
                    theEntity.setClawsForward(true);
                    ((INavigationFlying)theEntity.getNavigatorNew()).enableDirectTarget(true);
                    isCommittedToFinalRun = true;
                } else {
                    theEntity.transitionAIGoal(Goal.NONE);
                    endSwoop = true;
                }
            } else if (time > INITIAL_LINEUP_TIME) {
                double dYp = -(swoopTarget.getY() - theEntity.getY());
                if (dYp < 2.9) {
                    dYp = 0;
                }
                ((INavigationFlying)theEntity.getNavigatorNew()).setPitchBias(diveAngle * (float) (dYp / diveHeight), (float) (0.6D * (dYp / diveHeight)));
            }

        } else if (theEntity.distanceTo(swoopTarget) < strikeDistance) {
            theEntity.transitionAIGoal(Goal.FLYING_STRIKE);
            ((INavigationFlying)theEntity.getNavigatorNew()).enableDirectTarget(false);
            endSwoop = true;
        } else {
            double yawToTarget = Math.atan2(
                    swoopTarget.getZ() - theEntity.getZ(),
                    swoopTarget.getX() - theEntity.getX()
            ) * MathHelper.DEGREES_PER_RADIAN - 90;
            if (Math.abs(MathHelper.subtractAngles((float) yawToTarget, theEntity.getYaw())) > 90) {
                theEntity.transitionAIGoal(Goal.NONE);
                ((INavigationFlying)theEntity.getNavigatorNew()).enableDirectTarget(false);
                theEntity.setClawsForward(false);
                endSwoop = true;
            }
        }
    }

    private boolean isSwoopPathClear(LivingEntity target, float diveAngle) {
        double dRayY = 2;
        int hitCount = 0;
        double lowestCollide = theEntity.getY();
        for (double y = theEntity.getY() - dRayY; y > target.getY(); y -= dRayY) {
            double dist = Math.tan(90 + diveAngle) * (theEntity.getY() - y);
            BlockHitResult collide = theEntity.getWorld().raycast(new RaycastContext(new Vec3d(
                    -Math.sin(theEntity.getYaw() * MathHelper.RADIANS_PER_DEGREE) * dist,
                    y,
                    Math.cos(theEntity.getYaw() * MathHelper.RADIANS_PER_DEGREE) * dist
            ), target.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, theEntity));
            if (collide != null && collide.getType() != Type.MISS) {
                if (hitCount == 0) {
                    lowestCollide = y;
                }
                hitCount++;
            }
        }

        return isAcceptableDiveSpace(theEntity.getY(), lowestCollide, hitCount);
    }

    private boolean isFinalRunLinedUp() {
        Vec3d delta = swoopTarget.getPos().subtract(theEntity.getPos());
        double dXZ = delta.horizontalLength();
        double yawToTarget = Math.atan2(delta.x, delta.z) * MathHelper.DEGREES_PER_RADIAN - 90;
        double dYaw = MathHelper.subtractAngles((float) yawToTarget, theEntity.getYaw());
        if (dYaw < -finalRunArcLimit || dYaw > finalRunArcLimit) {
            return false;
        }
        double dPitch = Math.atan(delta.x / dXZ) * MathHelper.DEGREES_PER_RADIAN - theEntity.getPitch();
        return dPitch >= -finalRunArcLimit && dPitch <= finalRunArcLimit;
    }

    protected boolean isAcceptableDiveSpace(double entityPosY, double lowestCollideY, int hitCount) {
        return entityPosY - lowestCollideY >= minDiveClearanceY;
    }
}