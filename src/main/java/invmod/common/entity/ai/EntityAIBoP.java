package invmod.common.entity.ai;

import invmod.common.entity.EntityIMFlying;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;
import invmod.common.entity.MoveState;
import net.minecraft.entity.LivingEntity;

public class EntityAIBoP extends net.minecraft.entity.ai.goal.Goal {
    private static final int PATIENCE = 600;
    private static final int MIN_ATTACK_DISTANCE = 10;

    private final EntityIMFlying mob;

    private int timeWithGoal;
    private Goal lastGoal;

    public EntityAIBoP(EntityIMFlying entity) {
        mob = entity;
        lastGoal = entity.getAIGoal();
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void start() {
        timeWithGoal = 0;
    }

    @Override
    public void tick() {
        timeWithGoal++;

        if (mob.getAIGoal() != lastGoal) {
            lastGoal = mob.getAIGoal();
            timeWithGoal = 0;
        }

        LivingEntity lastTarget = mob.getTarget();

        if (lastTarget == null) {
            if (mob.getNexus() != null) {
                if (mob.getAIGoal() != Goal.BREAK_NEXUS) {
                    mob.transitionAIGoal(Goal.BREAK_NEXUS);
                }
            } else if (mob.getAIGoal() != Goal.CHILL) {
                mob.transitionAIGoal(Goal.CHILL);
                mob.getNavigatorNew().clearPath();
                ((INavigationFlying)mob.getNavigatorNew()).setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
                ((INavigationFlying)mob.getNavigatorNew()).setLandingPath();
            }
        } else if (mob.getAIGoal() == Goal.CHILL || mob.getAIGoal() == Goal.NONE) {
            mob.transitionAIGoal(shouldAttackTarget(lastTarget) ? Goal.MELEE_TARGET : Goal.STAY_AT_RANGE);
        }

        if (mob.getAIGoal() != Goal.STAY_AT_RANGE && mob.getAIGoal() == Goal.MELEE_TARGET && timeWithGoal > PATIENCE) {
            mob.transitionAIGoal(Goal.STAY_AT_RANGE);
        }
    }

    private boolean shouldAttackTarget(LivingEntity target) {
        return mob.getMoveState() != MoveState.FLYING
                && mob.distanceTo(target) < MIN_ATTACK_DISTANCE
                && mob.getRandom().nextFloat() > 0.3F;
    }
}