package com.invasion.entity.ai;

import java.util.Optional;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathNode;
import com.invasion.util.math.CoordsInt;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class IMMoveHelperSpider extends IMMoveHelper {
    private static final Direction[] DIRECTIONS = Direction.values();

    public IMMoveHelperSpider(EntityIMLiving par1EntityLiving) {
        super(par1EntityLiving);
    }

    private int getMoveDirection() {
        Path path = entity.getNavigatorNew().getPath();
        if (path != null && !path.isFinished()) {
            PathNode currentPoint = path.getPathPointFromIndex(path.getCurrentPathIndex());
            int pathLength = path.getCurrentPathLength();
            for (int i = path.getCurrentPathIndex(); i < pathLength; i++) {
                PathNode point = path.getPathPointFromIndex(i);
                if (point.pos.getX() > currentPoint.pos.getX()) {
                    return 0;
                }
                if (point.pos.getX() < currentPoint.pos.getX()) {
                    return 2;
                }
                if (point.pos.getZ() > currentPoint.pos.getZ()) {
                    return 4;
                }
                if (point.pos.getZ() < currentPoint.pos.getZ()) {
                    return 6;
                }
            }
        }
        return 0;
    }

    @Override
    protected Optional<Direction> getClimbFace(BlockPos pos) {
        pos = BlockPos.ofFloored(Vec3d.of(pos).subtract(entity.getWidth() * 0.5F, 0, entity.getWidth() * 0.5F));

        int index = getMoveDirection();

        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Vec3i offset : CoordsInt.OFFSET_ADJACENT_2) {
            BlockState state = entity.getWorld().getBlockState(mutable.set(pos).move(offset));
            if (state.isFullCube(entity.getWorld(), mutable)) {
                return Optional.of(DIRECTIONS[(index % 8) / 2]);
            }
            index++;
        }
        return Optional.empty();
    }
}