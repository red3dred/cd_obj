package invmod.common.entity.ai;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;
import invmod.common.entity.Path;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class EntityAIBirdFight<T extends LivingEntity> extends EntityAIMeleeFight<T> {
    private final EntityIMBird theEntity;
    private boolean wantsToRetreat;
    private boolean buffetedTarget;

    public EntityAIBirdFight(EntityIMBird entity, Class<? extends T> targetClass, int attackDelay, float retreatHealthLossPercent) {
        super(entity, targetClass, attackDelay, retreatHealthLossPercent);
        theEntity = entity;
    }

    @Override
    public void tick() {
        if (getAttackTime() == 0) {
            theEntity.setAttackingWithWings(isInStartMeleeRange());
        }
        super.tick();
    }

    @Override
    public void stop() {
        theEntity.setAttackingWithWings(false);
        super.stop();
    }

    @Override
    public void updatePath() {
        INavigationFlying nav = theEntity.getNavigatorNew();
        Entity target = mob.getTarget();
        if (target != nav.getTargetEntity()) {
            nav.clearPath();
            nav.setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
            Path path = nav.getPathToEntity(target, 0);
            if (path != null && path.getCurrentPathLength() > 1.6D * mob.distanceTo(target)) {
                nav.setMovementType(INavigationFlying.MoveType.MIXED);
            }
            nav.autoPathToEntity(target);
        }
    }

    @Override
    protected void updateDisengage() {
        if (!wantsToRetreat) {
            if (shouldLeaveMelee()) {
                wantsToRetreat = true;
            }
        } else if (buffetedTarget && mob.getAIGoal() == Goal.MELEE_TARGET) {
            mob.transitionAIGoal(Goal.LEAVE_MELEE);
        }
    }

    @Override
    protected void attackEntity(LivingEntity target) {
        theEntity.doMeleeSound();
        super.attackEntity(target);
        if (wantsToRetreat) {
            doWingBuffetAttack(target);
            buffetedTarget = true;
        }
    }

    protected boolean isInStartMeleeRange() {
        LivingEntity target = mob.getTarget();
        return target != null && mob.squaredDistanceTo(target) < MathHelper.square(mob.getWidth() + mob.getAttackRange() + 3.0D);
    }

    protected void doWingBuffetAttack(LivingEntity target) {
        target.takeKnockback(2, target.getX() - mob.getX(), target.getZ() - mob.getZ());
        target.getWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_BIG_FALL,
                target.getSoundCategory(), 1, 1);
    }
}