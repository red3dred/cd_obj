package com.invasion.entity.pathfinding;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.Heightmap.Type;

public interface ClimberUtil {
    static boolean isLadder(CollisionView world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.LADDER);
    }

    static boolean canPositionSupportLadder(WorldView world, BlockPos.Mutable pos, Direction facing) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.LADDER)
            || (PathingUtil.isAirOrReplaceable(state) && Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, facing).canPlaceAt(world, pos));
    }

    static Stream<Direction> getPossibleLadderOrientations(WorldView world, BlockPos.Mutable pos) {
        BlockState state = world.getBlockState(pos);
        if (!PathingUtil.isAirOrReplaceable(state)) {
            return Stream.empty();
        }
        return Direction.Type.HORIZONTAL.stream().filter(facing -> canPositionSupportLadder(world, pos, facing));
    }

    static Direction getOrientationFromNeighbors(CollisionView world, BlockPos.Mutable mutable, List<Direction> possibleOrientations) {
        BlockState above = world.getBlockState(mutable.move(Direction.UP));
        BlockState below = world.getBlockState(mutable.move(Direction.DOWN, 2));
        mutable.move(Direction.UP);

        if (above.isOf(Blocks.LADDER)) {
            Direction aboveDirection = above.get(LadderBlock.FACING);
            if (possibleOrientations.contains(aboveDirection)) {
                return aboveDirection;
            }
        }
        if (below.isOf(Blocks.LADDER)) {
            Direction belowDirection = below.get(LadderBlock.FACING);
            if (possibleOrientations.contains(belowDirection)) {
                return belowDirection;
            }
        }

        return possibleOrientations.get(0);
    }

    static int getWallHeight(WorldView world, BlockPos.Mutable mutable, Direction facing, int max) {
        return PathingUtil.scanVertically(world, mutable, max, pos -> canPositionSupportLadder(world, pos, facing));
    }

    static int getGapHeight(World world, BlockPos.Mutable mutable, int max) {
        int maxY = world.getTopY(Type.WORLD_SURFACE, mutable.getX(), mutable.getZ());
        return PathingUtil.scanVertically(world, mutable, max, pos -> {
            return pos.getY() < maxY && PathingUtil.isAirOrReplaceable(world.getBlockState(pos));
        });
    }

    static int getWallHeightPermittingGaps(World world, BlockPos.Mutable mutable, Direction facing, int maxWall, int maxGap) {
        BlockPos initial = mutable.toImmutable();
        int maxY = world.getTopY(Type.WORLD_SURFACE, mutable.getX(), mutable.getZ());

        int wallHeight = 0;
        int gapHeight = 0;

        int i = 0;
        for (; i < maxY; i++) {
            mutable.set(initial).move(Direction.UP, i);
            boolean isWall = canPositionSupportLadder(world, mutable, facing);
            boolean isGap = PathingUtil.isAirOrReplaceable(world.getBlockState(mutable.offset(facing)));

            if (isWall) {
                gapHeight = 0;
                if (++wallHeight >= maxWall) {
                    break;
                }
            } else if (isGap) {
                wallHeight = 0;
                if (++gapHeight >= maxGap) {
                    break;
                }
            }
        }

        return i;
    }
}
