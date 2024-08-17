package com.invasion.entity.ai.goal;

import com.invasion.entity.AbstractIMZombieEntity;

public class EntityAISprinpigman extends EntityAISprint<AbstractIMZombieEntity> {
    public EntityAISprinpigman(AbstractIMZombieEntity entity) {
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