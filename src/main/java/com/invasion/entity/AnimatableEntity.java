package com.invasion.entity;

import com.invasion.entity.ai.MoveState;

public interface AnimatableEntity {
    MoveState getMoveState();

    void setMoveState(MoveState moveState);
}
