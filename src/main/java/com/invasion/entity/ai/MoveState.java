package com.invasion.entity.ai;

public enum MoveState {
    STANDING,
    RUNNING,
    NONE,
    CLIMBING,
    FLYING;

    private static final MoveState[] VALUES = values();

    public static MoveState of(int id) {
        return VALUES[id % VALUES.length];
    }
}