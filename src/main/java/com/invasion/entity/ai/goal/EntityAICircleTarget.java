package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.pathfinding.INavigationFlying;

import net.minecraft.entity.Entity;

public class EntityAICircleTarget extends net.minecraft.entity.ai.goal.Goal {
    private static final int ATTACK_SEARCH_TIME = 400;
    private EntityIMFlying mob;

    private int patienceTime;
    private int patience;
    private float preferredHeight;
    private float preferredRadius;

    public EntityAICircleTarget(EntityIMFlying entity, int patience, float preferredHeight, float preferredRadius) {
        mob = entity;
        this.patience = patience;
        this.preferredHeight = preferredHeight;
        this.preferredRadius = preferredRadius;
    }

    @Override
    public boolean canStart() {
        return mob.hasGoal(Goal.STAY_AT_RANGE) && hasValidTarget();
    }

    @Override
    public boolean shouldContinue() {
        return mob.hasOrIsBetweenGoals(Goal.STAY_AT_RANGE, Goal.FIND_ATTACK_OPPORTUNITY) && hasValidTarget();
    }

    private boolean hasValidTarget() {
        Entity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        INavigationFlying nav = (INavigationFlying)mob.getNavigatorNew();
        nav.setMovementType(INavigationFlying.MoveType.PREFER_FLYING);
        nav.setCirclingPath(mob.getTarget(), this.preferredHeight, this.preferredRadius);

        int extraTime = Math.max(0, (int) (4 * nav.getDistanceToCirclingRadius()));
        this.patienceTime = (extraTime + mob.getRandom().nextInt(this.patience) + this.patience / 3);
    }

    @Override
    public void tick() {
        if (mob.hasGoal(Goal.STAY_AT_RANGE)) {
            if (--patienceTime <= 0) {
                mob.transitionAIGoal(Goal.FIND_ATTACK_OPPORTUNITY);
                patienceTime = ATTACK_SEARCH_TIME;
            }
        } else if (mob.isBetweenGoals(Goal.STAY_AT_RANGE, Goal.FIND_ATTACK_OPPORTUNITY)) {
            patienceTime--;
        }
    }
}