package com.invasion.entity.ai.goal;

import com.invasion.entity.NexusEntity;
import com.invasion.entity.PigmanEngineerEntity;

import net.minecraft.entity.mob.PathAwareEntity;

public class ProvideSupportGoal extends FollowEntityGoal<PigmanEngineerEntity> {
    private final boolean canHelp;

    public <E extends PathAwareEntity & NexusEntity> ProvideSupportGoal(E entity, float followDistance, boolean canHelp) {
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