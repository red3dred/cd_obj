package com.invasion.entity.pathfinding;

import net.minecraft.world.BlockView;

public interface IPathfindable {
    float getBlockPathCost(PathNode startNode, PathNode endNode, BlockView world);

    void getPathOptionsFromNode(BlockView world, PathNode node, PathfinderIM finder);
}