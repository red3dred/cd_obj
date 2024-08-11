package invmod.common.entity.ai;

import invmod.common.entity.EntityIMFlying;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;

public class EntityAIStabiliseFlying extends net.minecraft.entity.ai.goal.Goal {
    private static final int INITIAL_STABILISE_TIME = 50;

    private final EntityIMFlying theEntity;
    private final int stabiliseTime;

    private int time;

    public EntityAIStabiliseFlying(EntityIMFlying entity, int stabiliseTime) {
        theEntity = entity;
        this.stabiliseTime = stabiliseTime;
    }

    @Override
    public boolean canStart() {
        return theEntity.getAIGoal() == Goal.STABILISE;
    }

    @Override
    public boolean shouldContinue() {
        if (time < stabiliseTime) {
            return true;
        }

        theEntity.transitionAIGoal(Goal.NONE);
        theEntity.getNavigatorNew().setPitchBias(0, 0);
        return false;
    }

    @Override
    public void start() {
        time = 0;
        INavigationFlying nav = theEntity.getNavigatorNew();
        nav.clearPath();
        nav.setMovementType(INavigationFlying.MoveType.PREFER_FLYING);
        nav.setPitchBias(20.0F, 0.5F);
    }

    @Override
    public void tick() {
        if (++time == INITIAL_STABILISE_TIME) {
            theEntity.getNavigatorNew().setPitchBias(0, 0);
        }
    }
}