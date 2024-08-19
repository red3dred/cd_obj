package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMBird;
import com.invasion.entity.IHasAiGoals;
import net.minecraft.entity.LivingEntity;

public class EntityAIFlyingStrike extends net.minecraft.entity.ai.goal.Goal {
    private final EntityIMBird theEntity;

    public EntityAIFlyingStrike(EntityIMBird entity) {
        theEntity = entity;
    }

    @Override
    public boolean canStart() {
        return theEntity.hasAnyGoal(IHasAiGoals.Goal.FLYING_STRIKE, IHasAiGoals.Goal.SWOOP);
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        if (theEntity.hasGoal(IHasAiGoals.Goal.FLYING_STRIKE)) {
            doStrike();
        }
    }

    private void doStrike() {
        LivingEntity target = theEntity.getTarget();
        if (target == null) {
            theEntity.transitionAIGoal(IHasAiGoals.Goal.NONE);
            return;
        }

        float flyByChance = 1;
        float tackleChance = 0;
        float pickUpChance = 0;
        if (theEntity.getClawsForward()) {
            flyByChance = 0.5F;
            tackleChance = 100;
            pickUpChance = 1;
        }

        float pE = flyByChance + tackleChance + pickUpChance;
        float r = theEntity.getRandom().nextFloat();
        if (r <= flyByChance / pE) {
            doFlyByAttack(target);
            theEntity.transitionAIGoal(IHasAiGoals.Goal.STABILISE);
            theEntity.setClawsForward(false);
        } else if (r <= (flyByChance + tackleChance) / pE) {
            theEntity.transitionAIGoal(IHasAiGoals.Goal.TACKLE_TARGET);
            theEntity.setClawsForward(false);
        } else {
            theEntity.transitionAIGoal(IHasAiGoals.Goal.PICK_UP_TARGET);
        }
    }

    private void doFlyByAttack(LivingEntity entity) {
        this.theEntity.tryAttack(entity);
    }
}