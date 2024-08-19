package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.PigmanEngineerEntity;

public class WaitForSupportGoal extends FollowEntityGoal<PigmanEngineerEntity> {
    private final boolean canHelp;

    public WaitForSupportGoal(EntityIMLiving entity, float followDistance, boolean canHelp) {
        super(entity, PigmanEngineerEntity.class, followDistance);
        this.canHelp = canHelp;
    }

    @Override
    public void tick() {
        super.tick();
        if (canHelp && getTarget() != null) {
            getTarget().supportForTick(mob, 1);
        }
    }
}