package com.invasion.entity.ai.goal;

import com.invasion.entity.VultureEntity;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.FlyingNavigation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class EntityAIBirdFight<T extends LivingEntity> extends MeleeFightGoal<T, VultureEntity> {
    private final VultureEntity theEntity;
    private final EntityNavigation navigation;
    private final FlyingNavigation flyingNavigation;
    private boolean wantsToRetreat;
    private boolean buffetedTarget;

    public EntityAIBirdFight(VultureEntity entity, Class<? extends T> targetClass, int attackDelay, float retreatHealthLossPercent) {
        super(entity, targetClass, attackDelay, retreatHealthLossPercent);
        theEntity = entity;
        navigation = entity.getNavigation();
        flyingNavigation = (FlyingNavigation)theEntity.getNavigatorNew();
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
        Entity target = mob.getTarget();
        if (target != mob.getNavigatorNew().getTargetEntity()) {
            navigation.stop();
            ((FlyingNavigation)theEntity.getNavigatorNew()).setMovementType(FlyingNavigation.MoveType.PREFER_WALKING);
            Path path = theEntity.getNavigation().findPathTo(target, MathHelper.ceil(1.6D * mob.distanceTo(target)));
            if (path != null && path.getLength() > 1.6D * mob.distanceTo(target)) {
                ((FlyingNavigation)theEntity.getNavigatorNew()).setMovementType(FlyingNavigation.MoveType.MIXED);
            }
            flyingNavigation.autoPathToEntity(target);
        }
    }

    @Override
    protected void updateDisengage() {
        if (!wantsToRetreat) {
            if (shouldLeaveMelee()) {
                wantsToRetreat = true;
            }
        } else if (buffetedTarget && mob.hasGoal(HasAiGoals.Goal.MELEE_TARGET)) {
            mob.transitionAIGoal(HasAiGoals.Goal.LEAVE_MELEE);
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
        return target != null && mob.isInRange(target, mob.getWidth() + 3);
    }

    protected void doWingBuffetAttack(LivingEntity target) {
        target.takeKnockback(2, target.getX() - mob.getX(), target.getZ() - mob.getZ());
        target.getWorld().playSoundAtBlockCenter(target.getBlockPos(), SoundEvents.ENTITY_GENERIC_BIG_FALL, target.getSoundCategory(), 1, 1, true);
    }
}