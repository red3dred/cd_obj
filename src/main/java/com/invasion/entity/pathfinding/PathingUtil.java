package com.invasion.entity.pathfinding;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public interface PathingUtil {
    Set<PathNodeType> AVOIDED_TYPES = Set.of(
            PathNodeType.DAMAGE_CAUTIOUS,
            PathNodeType.DAMAGE_OTHER,
            PathNodeType.DANGER_OTHER,
            PathNodeType.UNPASSABLE_RAIL
    );
    Set<PathNodeType> FIRE_DAMAGE_TYPES = Set.of(
            PathNodeType.DAMAGE_FIRE,
            PathNodeType.DANGER_FIRE,
            PathNodeType.LAVA
    );
    Set<PathNodeType> WATER_DAMAGE_TYPES = Set.of(
            PathNodeType.WATER,
            PathNodeType.WATER_BORDER
    );

    static boolean hasAdjacentLadder(BlockView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction offset : Direction.Type.HORIZONTAL) {
            if (isLadder(world.getBlockState(mutable.set(pos).move(offset)))) {
                return true;
            }
        }
        return false;
    }

    static boolean isLadder(BlockState state) {
        return state.isIn(BlockTags.CLIMBABLE);
    }

    static boolean isAirOrReplaceable(BlockState state) {
        return state.isAir() || state.isReplaceable();
    }

    static boolean shouldAvoidBlock(MobEntity entity, BlockPos pos) {
        if (entity.isInvulnerable()) {
            return false;
        }

        PathNodeType type = LandPathNodeMaker.getLandNodeType(entity, pos);
        return AVOIDED_TYPES.contains(type)
                || (!entity.isFireImmune() && FIRE_DAMAGE_TYPES.contains(type))
                || ( entity.canFreeze() && type == PathNodeType.DANGER_POWDER_SNOW)
                || (!entity.canBreatheInWater() && WATER_DAMAGE_TYPES.contains(type));
    }

    static int scanVertically(WorldView world, BlockPos.Mutable mutable, int max, Predicate<BlockPos.Mutable> test) {
        int initialY = mutable.getY();
        int height = 0;
        while (height < max && test.test(mutable.setY(initialY + height))) {
            height++;
        }
        return height;
    }
}
