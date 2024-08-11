package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityAISprint extends net.minecraft.entity.ai.goal.Goal {
    protected final EntityIMLiving theEntity;

    private int updateTimer;
    private int timer;
    private int missingTarget;

    private boolean isExecuting = true;
    private boolean isSprinting;
    private boolean isInWindup;

    protected Vec3d lastPos = Vec3d.ZERO;

    public EntityAISprint(EntityIMLiving entity) {
        theEntity = entity;
    }

    @Override
    public boolean canStart() {
        if (--updateTimer <= 0) {
            updateTimer = 20;
            if ((theEntity.getTarget() != null && theEntity.getVisibilityCache().canSee(theEntity.getTarget())) || isSprinting) {
                return true;
            }

            isExecuting = false;
            return false;
        }

        return isExecuting;
    }

    @Override
    public void start() {
        isExecuting = true;
        timer = 60;
    }

    @Override
    public void tick() {
        if (this.isSprinting) {
            Entity target = theEntity.getTarget();
            if (!theEntity.isSprinting() || target == null || (missingTarget > 0 && ++missingTarget > 20)) {
                endSprint();
                return;
            }

            double dX = target.getX() - theEntity.getX();
            double dZ = target.getZ() - theEntity.getZ();
            double dAngle = MathHelper.wrapDegrees(Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90 - theEntity.getYaw());
            if (dAngle > 60) {
                theEntity.setTurnRate(2);
                missingTarget = 1;
            }

            if (theEntity.squaredDistanceTo(lastPos) < 0.0009D) {
                crash();
                return;
            }

            lastPos = theEntity.getPos();
        }

        if (--timer <= 0) {
            if (!isInWindup) {
                if (!isSprinting) {
                    startSprint();
                } else {
                    endSprint();
                }
            } else {
                sprint();
            }
        }
    }

    protected void startSprint() {
        Entity target = theEntity.getTarget();
        if ((target == null) || (target.getY() - theEntity.getY() >= 1)) {
            return;
        }
        double dX = target.getX() - theEntity.getX();
        double dZ = target.getZ() - theEntity.getZ();
        double dAngle = MathHelper.wrapDegrees(Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90 - theEntity.getYaw());
        if (dAngle < 10) {
            isInWindup = true;
            timer = 20;
            theEntity.setMoveSpeedStat(0);
        } else {
            timer = 10;
        }
    }

    protected void sprint() {
        isInWindup = false;
        isSprinting = true;
        missingTarget = 0;
        timer = 35;

        theEntity.resetMoveSpeed();
        theEntity.setMovementSpeed(theEntity.getMovementSpeed() * 2.3F);
        theEntity.setSprinting(true);
        theEntity.setTurnRate(4.9F);
        theEntity.setAttacking(false);
    }

    protected void endSprint() {
        isSprinting = false;
        timer = 180;
        theEntity.resetMoveSpeed();
        theEntity.setTurnRate(30);
        theEntity.setSprinting(false);
    }

    protected void crash() {
        theEntity.stunEntity(40);
        theEntity.damage(theEntity.getDamageSources().generic(), 5);
        theEntity.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1F, 0.6F);
        endSprint();
    }
}