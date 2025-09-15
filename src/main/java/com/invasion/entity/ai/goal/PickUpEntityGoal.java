package com.invasion.entity.ai.goal;

import org.joml.Vector3f;

import com.invasion.entity.VultureEntity;
import com.invasion.entity.pathfinding.FlyingNavigation;
import com.invasion.entity.HasAiGoals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PickUpEntityGoal extends net.minecraft.entity.ai.goal.Goal {
    private final VultureEntity theEntity;

    private final Vector3f pickupPoint;

    private final float pickupRangeY;
    private final float pickupRangeXZ;

    private final float abortAngleYaw;
    private final float abortAnglePitch;

    private int time;
    private int holdTime = 70;
    private int abortTime;
    private boolean isHoldingEntity;

    public PickUpEntityGoal(VultureEntity entity, Vector3f pickupPoint, float pickupRangeY, float pickupRangeXZ, int abortTime, float abortAngleYaw, float abortAnglePitch) {
        this.theEntity = entity;
        this.pickupPoint = pickupPoint;
        this.pickupRangeY = pickupRangeY;
        this.pickupRangeXZ = pickupRangeXZ;
        this.abortTime = abortTime;
        this.abortAngleYaw = abortAngleYaw;
        this.abortAnglePitch = abortAnglePitch;
    }

    @Override
    public boolean canStart() {
        return theEntity.hasGoal(HasAiGoals.Goal.PICK_UP_TARGET) || theEntity.hasPassengers();
    }

    @Override
    public void start() {
        isHoldingEntity = theEntity.hasPassengers();
        time = 0;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = theEntity.getTarget();
        if (target != null && target.isAlive()) {
            if (!isHoldingEntity) {
                if (time > abortTime && isLinedUp(target)) {
                    return true;
                }
            } else if (theEntity.isConnectedThroughVehicle(target)) {
                return true;
            }
        }
        theEntity.transitionAIGoal(HasAiGoals.Goal.NONE);
        theEntity.setClawsForward(false);
        return false;
    }

    @Override
    public void tick() {
        time++;
        if (!isHoldingEntity) {
            LivingEntity target = theEntity.getTarget();
            double dY = target.prevY - theEntity.prevY;
            if (Math.abs(dY - pickupPoint.y) < pickupRangeY) {
                double dAngle = theEntity.prevY * MathHelper.RADIANS_PER_DEGREE;
                double sinF = Math.sin(dAngle);
                double cosF = Math.cos(dAngle);
                double x = pickupPoint.x * cosF - pickupPoint.z * sinF;
                double z = pickupPoint.z * cosF + pickupPoint.x * sinF;

                double dX = target.prevX - (x + theEntity.prevX);
                double dZ = target.prevZ - (z + theEntity.prevZ);
                double dXZ = Math.sqrt(dX * dX + dZ * dZ);

                if (dXZ < pickupRangeXZ) {
                    target.startRiding(theEntity);
                    isHoldingEntity = true;
                    time = 0;
                    theEntity.getNavigation().stop();
                    ((FlyingNavigation)theEntity.getNavigatorNew()).setPitchBias(20, 1.5F);
                }
            }
        } else if (time == 45) {
            ((FlyingNavigation)theEntity.getNavigatorNew()).setPitchBias(0, 0);
        } else if (time > holdTime) {
            theEntity.getTarget().stopRiding();
        }
    }

    private boolean isLinedUp(Entity target) {
        Vec3d delta = target.getPos().subtract(theEntity.getPos());
        double dXZ = delta.horizontalLength();
        double yawToTarget = Math.atan2(delta.z, delta.x) * MathHelper.DEGREES_PER_RADIAN - 90;
        double dYaw = MathHelper.subtractAngles((float)yawToTarget, theEntity.getYaw());
        if (dYaw < -abortAngleYaw || dYaw > abortAngleYaw) {
            return false;
        }
        double dPitch = Math.atan(delta.y / dXZ) * MathHelper.DEGREES_PER_RADIAN - theEntity.getPitch();
        return dPitch >= -abortAnglePitch && dPitch <= abortAnglePitch;
    }
}