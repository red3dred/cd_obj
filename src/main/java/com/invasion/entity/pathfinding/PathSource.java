package com.invasion.entity.pathfinding;

import com.invasion.entity.NexusEntity;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

@Deprecated
public interface PathSource {

    int getSearchDepth();

    void setSearchDepth(int depth);

    int getQuickFailDepth();

    void setQuickFailDepth(int depth);

    Path createPath(IMPathNodeMaker pather, BlockPos from, BlockPos to, float targetRadius, float maxSearchRange, BlockView world);

    default Path createPath(NexusEntity entity, BlockPos pos, float targetRadius, float maxSearchRange, BlockView terrainMap) {
        return createPath(
                entity.getNavigatorNew().getActor(),
                getPathBegin(entity),
                pos.add(MathHelper.floor(0.5F - entity.asEntity().getWidth() * 0.5F), 0, MathHelper.floor(0.5F - entity.asEntity().getWidth() * 0.5F)),
                targetRadius, maxSearchRange, terrainMap);
    }

    static BlockPos getPathBegin(NexusEntity entity) {
        if (entity.asEntity().getWidth() <= 1) {
            return entity.getBlockPos();
        }

        return BlockPos.ofFloored(entity.getBoundingBox().getMinPos());
    }

    public enum PathPriority {
        LOW, MEDIUM, HIGH
    }
}