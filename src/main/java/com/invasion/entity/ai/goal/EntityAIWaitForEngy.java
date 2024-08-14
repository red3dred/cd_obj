package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.EntityIMPigEngy;

public class EntityAIWaitForEngy extends EntityAIFollowEntity<EntityIMPigEngy> {
    private final boolean canHelp;

    public EntityAIWaitForEngy(EntityIMLiving entity, float followDistance, boolean canHelp) {
        super(entity, EntityIMPigEngy.class, followDistance);
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