package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class LookAtTargetGoal extends Goal {
    private final MobEntity mob;

    public LookAtTargetGoal(MobEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return mob.getTarget() != null;
    }

    @Override
    public void tick() {
        mob.getLookControl().lookAt(mob.getTarget(), 2, 2);
    }
}