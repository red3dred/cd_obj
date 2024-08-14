package com.invasion.entity.pathfinding;

import com.invasion.IPathfindable;
import com.invasion.entity.EntityIMLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public interface IPathSource {

    int getSearchDepth();

    void setSearchDepth(int depth);

    int getQuickFailDepth();

    void setQuickFailDepth(int depth);

    Path createPath(IPathfindable findable, BlockPos from, BlockPos to, float targetRadius, float maxSearchRange, BlockView world);

    default Path createPath(EntityIMLiving entity, Entity target, float targetRadius, float maxSearchRange, BlockView terrainMap) {
        return createPath(entity, getPathingPosition(entity, target), targetRadius, maxSearchRange, terrainMap);
    }

    default Path createPath(EntityIMLiving entity, BlockPos pos, float targetRadius, float maxSearchRange, BlockView terrainMap) {
        return createPath(entity,
                getPathBegin(entity),
                pos.add(MathHelper.floor(0.5F - entity.getWidth() * 0.5F), 0, MathHelper.floor(0.5F - entity.getWidth() * 0.5F)),
                targetRadius, maxSearchRange, terrainMap);
    }

    default void createPath(IPathResult path, IPathfindable findable, BlockPos from, BlockPos to, float maxSearchRange, BlockView world) {

    }

    default void createPath(IPathResult path, EntityIMLiving entity, Entity target, float maxSearchRange, BlockView world) {

    }

    default void createPath(IPathResult path, EntityIMLiving entity, BlockPos pos, float maxSearchRange, BlockView world) {

    }

    default boolean canPathfindNice(PathPriority priority, float maxSearchRange, int searchDepth, int quickFailDepth) {
        return true;
    }

    static BlockPos getPathingPosition(Entity entity, Entity target) {
        return new BlockPos(
                MathHelper.floor(target.getX() + 0.5D - entity.getWidth() * 0.5F),
                MathHelper.floor(target.getY()),
                MathHelper.floor(target.getZ() + 0.5D - entity.getWidth() * 0.5F)
        );
    }

    static BlockPos getPathBegin(EntityIMLiving entity) {
        if (entity.getWidth() <= 1) {
            return entity.getBlockPos();
        }

        return BlockPos.ofFloored(entity.getBoundingBox().getMinPos());
    }

    public interface IPathResult {
        void pathCompleted(Path path);
    }

    public enum PathPriority {
        LOW, MEDIUM, HIGH
    }
}