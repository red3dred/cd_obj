package com.invasion.entity.ai.goal;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.EntityIMZombiePigman;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityAICharge<T extends LivingEntity> extends EntityAIMoveToEntity<T> {
    @Nullable
    protected LivingEntity target;

    protected Vec3d chargePos = Vec3d.ZERO;

    protected float speed;
    protected int windup;
    protected boolean hasAttacked;

    protected int chargeDelay = 100;
    protected int runTime = 15;

    public EntityAICharge(EntityIMLiving entity, Class<? extends T> targetClass, float f) {
        super(entity, targetClass);
        this.speed = f;
    }

    @Override
    public boolean canStart() {

        if (chargeDelay > 0) {
            chargeDelay--;
            return false;
        }

        target = mob.getTarget();
        if (target == null || target.isRemoved() || target.isDead() || !mob.isOnGround()) {
            return false;
        }
        double distance = Math.sqrt(mob.squaredDistanceTo(target));
        if (distance < 5 || distance > 20) {
            return false;
        }

        chargePos = findChargePoint(mob, target, 6);

        return mob.getRandom().nextInt(1) == 0;
    }

    @Override
    public void start() {
        windup = (15 + mob.getRandom().nextInt(25));
    }

    @Override
    public boolean shouldContinue() {
        if (windup == 0 && runTime > 0) {
            runTime--;
        }
        return windup > 0 || runTime > 0;
    }

    @Override
    public void tick() {
        mob.getLookControl().lookAt(chargePos.x, chargePos.y, chargePos.z, 10.0F, /* mob.getTurnRate()*/ 10);
        if (windup > 0) {
            if (--windup == 0) {
                mob.getNavigation().startMovingTo(chargePos.getX(), chargePos.getY(), chargePos.getZ(), speed);
            } else {
                mob.limbAnimator.setSpeed(mob.limbAnimator.getSpeed() + 0.8F);
                if (mob instanceof EntityIMZombiePigman pig) {
                    pig.setCharging(true);
                }
            }
        }

        if (!hasAttacked && mob.squaredDistanceTo(chargePos) <= MathHelper.square(mob.getWidth() * 2.1F)) {
            hasAttacked = true;
            mob.tryAttack(target);
        }
    }

    @Override
    public void stop() {
        windup = 0;
        target = null;
        hasAttacked = false;
        chargeDelay = 100;
        runTime = 15;
        if (mob instanceof EntityIMZombiePigman pig) {
            pig.setCharging(false);
        }
    }

    protected Vec3d findChargePoint(Entity attacker, Entity target, double overshoot) {
        Vec3d pos = mob.getPos();
        Vec3d delta = target.getPos().subtract(pos).multiply(1, 0, 1);
        float theta = (float) Math.atan2(delta.getX(), delta.getZ());
        double distance = delta.length() + overshoot;
        // Cylindrical to Cartesian
        return pos.add(
                distance * MathHelper.cos(theta),
                0,
                distance * MathHelper.sin(theta)
        );
    }
}
