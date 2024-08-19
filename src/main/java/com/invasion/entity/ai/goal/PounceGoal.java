package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMSpider;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PounceGoal extends Goal {
    private final EntityIMSpider theEntity;
    private final float minPower;
    private final float maxPower;

    private boolean isPouncing;
    private int pounceTimer;
    private int cooldown;

    public PounceGoal(EntityIMSpider entity, float minPower, float maxPower, int cooldown) {
        this.theEntity = entity;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.cooldown = cooldown;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = theEntity.getTarget();
        return --pounceTimer <= 0 && target != null && theEntity.getVisibilityCache().canSee(target) && theEntity.isOnGround();
    }

    public boolean continueExecuting() {
        return this.isPouncing;
    }

    public void startExecuting() {
        if (pounce(theEntity.getTarget().getPos())) {
            theEntity.setAirborneTime(0);
            isPouncing = true;
            theEntity.getNavigatorNew().haltForTick();
        } else {
            isPouncing = false;
        }
    }

    public void updateTask() {
        theEntity.getNavigatorNew().haltForTick();
        int airborneTime = theEntity.getAirborneTime();
        if (airborneTime > 20 && theEntity.isOnGround()) {
            isPouncing = false;
            pounceTimer = cooldown;
            theEntity.setAirborneTime(0);
            theEntity.getNavigatorNew().clearPath();
        } else {
            theEntity.setAirborneTime(airborneTime + 1);
        }
    }

    protected boolean pounce(Vec3d pos) {
        Vec3d delta = pos.subtract(theEntity.getPos());
        double dXZ = delta.horizontalLength();
        double a = Math.atan(delta.y / dXZ);
        if (Math.abs(a) > 0.7853981633974483D) {
            double radius = (dXZ / ((1 - Math.tan(a)) / Math.cos(a))) * theEntity.getFinalGravity();
            double power = 1D / Math.sqrt(1D / radius);
            if (power > minPower && power < maxPower) {
                double distance = MathHelper.SQUARE_ROOT_OF_TWO * dXZ;
                theEntity.addVelocity(
                        (power * delta.x / distance),
                        (power * dXZ / distance),
                        (power * delta.z / distance)
                );
                return true;
            }
        }
        return false;
    }
}