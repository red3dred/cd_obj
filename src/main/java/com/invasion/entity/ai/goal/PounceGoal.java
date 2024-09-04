package com.invasion.entity.ai.goal;

import com.invasion.entity.NexusSpiderEntity;

import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PounceGoal extends Goal {
    private final NexusSpiderEntity theEntity;
    private final float minPower;
    private final float maxPower;

    private boolean isPouncing;
    private int pounceTimer;
    private int cooldown;

    private int airborneTime;

    public PounceGoal(NexusSpiderEntity entity, float minPower, float maxPower, int cooldown) {
        this.theEntity = entity;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.cooldown = cooldown;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = theEntity.getTarget();
        return --pounceTimer <= 0
                && target != null
                && theEntity.getVisibilityCache().canSee(target)
                && theEntity.isOnGround();
    }

    @Override
    public boolean shouldContinue() {
        return isPouncing;
    }

    @Override
    public void start() {
        if (pounce(theEntity.getTarget().getEyePos())) {
            airborneTime = 0;
            isPouncing = true;
            theEntity.getNavigatorNew().haltForTick();
        } else {
            isPouncing = false;
        }
    }

    @Override
    public void tick() {
        theEntity.getNavigatorNew().haltForTick();
        if (airborneTime > 20 && theEntity.isOnGround()) {
            isPouncing = false;
            pounceTimer = cooldown;
            airborneTime = 0;
            theEntity.getNavigation().stop();
        } else {
            airborneTime++;
        }
    }

    protected boolean pounce(Vec3d pos) {
        Vec3d delta = pos.subtract(theEntity.getPos());
        double dXZ = delta.horizontalLength();
        double a = Math.atan(delta.y / dXZ);

        if (Math.abs(a) > 0.4853981633974483D) {
            double radius = (dXZ / ((1 - Math.tan(a)) / Math.cos(a))) * theEntity.getFinalGravity();
            double power = 1D / Math.sqrt(1D / radius);

            if (power > minPower && power < maxPower) {
                double distance = MathHelper.SQUARE_ROOT_OF_TWO * dXZ;
                theEntity.addVelocity(
                        (power * delta.x / distance),
                        (power * dXZ / distance),
                        (power * delta.z / distance)
                );
                theEntity.lookAt(EntityAnchor.EYES, pos);
                return true;
            }
        }
        return false;
    }
}