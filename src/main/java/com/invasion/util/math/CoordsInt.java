package com.invasion.util.math;

import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record CoordsInt(int x, int y, int z) implements IPosition {
    public static final int EAST = 0;
    public static final int WEST = 1;
    public static final int SOUTH = 2;
    public static final int NORTH = 3;

    public static final Direction[] CARDINAL_DIRECTIONS = { Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH };

    @Deprecated
    public static final int[] offsetAdjX = { 1, -1, 0,  0 };
    @Deprecated
    public static final int[] offsetAdjZ = { 0,  0, 1, -1 };

    public static final List<BlockPos> ZERO = List.of();
    public static final List<BlockPos> OFFSET_ADJACENT = List.of(
            new BlockPos( 1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos( 0, 0, 1),
            new BlockPos( 0, 0, -1)
    );
    public static final List<BlockPos> OFFSET_ADJACENT_2 = List.of(
            new BlockPos( 2, 0, 0),
            new BlockPos( 2, 0, 1),
            new BlockPos(-1, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos( 1, 0, 2),
            new BlockPos( 0, 0, 2),
            new BlockPos( 0, 0, -1),
            new BlockPos( 1, 0, -1)
    );
    public static final List<BlockPos> OFFSET_RING = List.of(
            new BlockPos( 1, 0, 1),
            new BlockPos( 0, 0, 1),
            new BlockPos(-1, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(-1, 0,-1),
            new BlockPos( 0, 0,-1),
            new BlockPos( 1, 0, 0)
    );

    public CoordsInt(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getXCoord() {
        return this.x;
    }

    @Override
    public int getYCoord() {
        return this.y;
    }

    @Override
    public int getZCoord() {
        return this.z;
    }
}