package com.invasion.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;

public class ExplodeGoal extends Goal {

    @Override
    public boolean canStart() {
        return false;
    }
}
