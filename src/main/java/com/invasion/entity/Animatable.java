package com.invasion.entity;

import com.invasion.entity.ai.MoveState;

public interface Animatable {
    MoveState getMoveState();

    void setMoveState(MoveState moveState);
}
