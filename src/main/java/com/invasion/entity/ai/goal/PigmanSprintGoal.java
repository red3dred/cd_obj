package com.invasion.entity.ai.goal;

import com.invasion.entity.AbstractIMZombieEntity;

public class PigmanSprintGoal extends SprintGoal<AbstractIMZombieEntity> {
    public PigmanSprintGoal(AbstractIMZombieEntity entity) {
        super(entity);
    }

    @Override
    protected void startSprint() {
        theEntity.updateAnimation(true);
        super.startSprint();
    }

    @Override
    protected void endSprint() {
        theEntity.updateAnimation(true);
        super.endSprint();
    }
}