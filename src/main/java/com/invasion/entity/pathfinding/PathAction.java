package com.invasion.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.Direction;

public enum PathAction {
    NONE(Type.NONE, null),
    LADDER_UP(Type.LADDER, Direction.UP),
    BRIDGE(Type.BRIDGE, null),
    SWIM(Type.SWIM, null),
    DIG(Type.DIG, null),
    LADDER_UP_PX(Type.LADDER, Direction.EAST),
    LADDER_UP_NX(Type.LADDER, Direction.WEST),
    LADDER_UP_PZ(Type.LADDER, Direction.SOUTH),
    LADDER_UP_NZ(Type.LADDER, Direction.NORTH),
    LADDER_TOWER_UP_PX(Type.TOWER, Direction.EAST),
    LADDER_TOWER_UP_NX(Type.TOWER, Direction.WEST),
    LADDER_TOWER_UP_PZ(Type.TOWER, Direction.SOUTH),
    LADDER_TOWER_UP_NZ(Type.TOWER, Direction.NORTH),
    SCAFFOLD_UP(Type.SCAFFOLD, Direction.UP);

    public static final PathAction[] ladderTowerIndexOrient = {
            LADDER_TOWER_UP_PX, LADDER_TOWER_UP_NX, LADDER_TOWER_UP_PZ, LADDER_TOWER_UP_NZ
    };
    public static final PathAction[] ladderIndexOrient = {
            LADDER_UP_PX, LADDER_UP_NX, LADDER_UP_PZ, LADDER_UP_NZ
    };

    public static PathAction getLadderActionForDirection(Direction direction) {
        return switch (direction) {
            case EAST -> LADDER_UP_PX;
            case WEST -> LADDER_UP_NX;
            case SOUTH -> LADDER_UP_PZ;
            case NORTH -> LADDER_UP_NZ;
            default -> NONE;
        };
    }

    private final Type type;
    private final Direction direction;

    PathAction(Type type, Direction direction) {
        this.type = type;
        this.direction = direction;
    }

    public Type getType() {
        return type;
    }

    @Nullable
    public Direction getBuildDirection() {
        return direction;
    }

    public boolean isHorizontal() {
        return direction != null && direction.getAxis().isHorizontal();
    }

    public enum Type {
        NONE,
        LADDER,
        BRIDGE,
        DIG,
        SWIM,
        TOWER,
        SCAFFOLD
    }
}