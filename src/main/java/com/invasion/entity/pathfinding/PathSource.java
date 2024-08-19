package com.invasion.entity.pathfinding;

import com.invasion.entity.EntityIMLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public interface PathSource {

    int getSearchDepth();

    void setSearchDepth(int depth);

    int getQuickFailDepth();

    void setQuickFailDepth(int depth);

    Path createPath(IPathfindable findable, BlockPos from, BlockPos to, float targetRadius, float maxSearchRange, BlockView world);

    default Path createPath(EntityIMLiving entity, BlockPos pos, float targetRadius, float maxSearchRange, BlockView terrainMap) {
        return createPath(entity.getNavigatorNew().getActor(),
                getPathBegin(entity),
                pos.add(MathHelper.floor(0.5F - entity.getWidth() * 0.5F), 0, MathHelper.floor(0.5F - entity.getWidth() * 0.5F)),
                targetRadius, maxSearchRange, terrainMap);
    }

    static BlockPos getPathBegin(EntityIMLiving entity) {
        if (entity.getWidth() <= 1) {
            return entity.getBlockPos();
        }

        return BlockPos.ofFloored(entity.getBoundingBox().getMinPos());
    }

    public enum PathPriority {
        LOW, MEDIUM, HIGH
    }
}